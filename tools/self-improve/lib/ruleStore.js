'use strict';
// CRUD + lifecycle transitions over memory-bank/rules.json (source of truth)
// and memory-bank/rules.md (generated render of ACCEPTED rules -- this is
// the file the SessionStart hook injects, so it only ever contains rules
// that are currently in force).
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const STATUSES = ['proposed', 'accepted', 'unused', 'archived'];
const IDLE_DAYS_TO_UNUSED = 60;
const IDLE_DAYS_TO_ARCHIVE = 60;

function storePaths(repoRoot) {
  return {
    json: path.join(repoRoot, 'memory-bank', 'rules.json'),
    md: path.join(repoRoot, 'memory-bank', 'rules.md'),
  };
}

function load(repoRoot) {
  const { json } = storePaths(repoRoot);
  if (!fs.existsSync(json)) return { rules: [] };
  try {
    const parsed = JSON.parse(fs.readFileSync(json, 'utf8'));
    if (!Array.isArray(parsed.rules)) return { rules: [] };
    return parsed;
  } catch {
    return { rules: [] };
  }
}

function normalizeText(text) {
  return text.toLowerCase().replace(/[^a-z0-9 ]/g, '').replace(/\s+/g, ' ').trim();
}

function findByNormalizedText(store, text) {
  const norm = normalizeText(text);
  return store.rules.find((r) => normalizeText(r.text) === norm);
}

function findById(store, id) {
  return store.rules.find((r) => r.id === id);
}

function newId() {
  return 'r-' + crypto.randomBytes(4).toString('hex');
}

function transition(rule, toStatus, note, at) {
  if (!STATUSES.includes(toStatus)) throw new Error(`unknown status: ${toStatus}`);
  rule.statusHistory.push({ at, from: rule.status, to: toStatus, note: note || null });
  rule.status = toStatus;
  if (toStatus === 'accepted') rule.acceptedAt = at;
}

// Adds a newly mined rule as "proposed", unless a rule with matching
// (normalized) text already exists:
//  - already proposed/accepted -> no-op, returns the existing rule
//  - unused/archived -> "pattern re-emerges -> reinstate": jump straight
//    back to accepted instead of re-proposing from scratch
function proposeRule(store, { text, rationale, method, trigger, sessionIds }, at) {
  const existing = findByNormalizedText(store, text);
  if (existing) {
    if (existing.status === 'unused' || existing.status === 'archived') {
      transition(existing, 'accepted', 'reinstated: pattern re-emerged in new session(s)', at);
      existing.source.sessionIds = Array.from(new Set([...existing.source.sessionIds, ...sessionIds]));
      return { rule: existing, action: 'reinstated' };
    }
    return { rule: existing, action: 'duplicate' };
  }

  const rule = {
    id: newId(),
    status: 'proposed',
    text,
    rationale: rationale || null,
    method: method || 'manual', // 'expel' | 'reflexion' | 'manual'
    trigger: trigger || { type: 'manual', tool: null, pattern: null },
    source: { sessionIds: sessionIds || [], extractedAt: at },
    createdAt: at,
    acceptedAt: null,
    lastHitAt: null,
    hitCount: 0,
    statusHistory: [{ at, from: null, to: 'proposed', note: null }],
  };
  store.rules.push(rule);
  return { rule, action: 'proposed' };
}

function daysBetween(isoA, isoB) {
  return (new Date(isoB).getTime() - new Date(isoA).getTime()) / 86400000;
}

// Recomputes hitCount/lastHitAt for every accepted/unused rule against a set
// of parsed sessions (see lib/transcripts.js), then applies the idle
// transitions: accepted -> unused (60d+ no hits) -> archived (60d+ more).
// Returns a list of { rule, transitionedTo } for anything that moved.
function sweep(store, sessions, now) {
  const moved = [];
  for (const rule of store.rules) {
    if (rule.status !== 'accepted' && rule.status !== 'unused') continue;

    const since = rule.lastHitAt || rule.acceptedAt;
    const relevant = sessions.filter((s) => s.startedAt && since && s.startedAt > since);
    const hits = relevant.filter((s) => ruleMatchesSession(rule, s));
    if (hits.length) {
      rule.hitCount += hits.length;
      rule.lastHitAt = hits.reduce((max, s) => (s.startedAt > max ? s.startedAt : max), hits[0].startedAt);
    }

    const idleSince = rule.lastHitAt || rule.acceptedAt;
    const idleDays = idleSince ? daysBetween(idleSince, now) : 0;

    if (rule.status === 'accepted' && idleDays >= IDLE_DAYS_TO_UNUSED) {
      transition(rule, 'unused', `idle ${Math.floor(idleDays)}d, no hits`, now);
      moved.push({ rule, transitionedTo: 'unused' });
    } else if (rule.status === 'unused') {
      const unusedSince = rule.statusHistory[rule.statusHistory.length - 1].at;
      const unusedDays = daysBetween(unusedSince, now);
      if (unusedDays >= IDLE_DAYS_TO_ARCHIVE) {
        transition(rule, 'archived', `idle ${Math.floor(unusedDays)}d since marked unused`, now);
        moved.push({ rule, transitionedTo: 'archived' });
      }
    }
  }
  return moved;
}

function ruleMatchesSession(rule, session) {
  const t = rule.trigger || {};
  if (t.type === 'tool_error' && t.tool) {
    return Object.keys(session.errorsByTool || {}).includes(t.tool);
  }
  if (t.type === 'keyword' && t.pattern) {
    const haystack = `${session.firstUserText} ${session.lastAssistantText}`.toLowerCase();
    return haystack.includes(t.pattern.toLowerCase());
  }
  // manual rules with no trigger: never auto-detected as "hit"; only a
  // human running self:approve/self:review keeps them alive deliberately.
  return false;
}

function renderRulesMd(store) {
  const accepted = store.rules.filter((r) => r.status === 'accepted');
  const lines = [
    '# Accepted Rules',
    '',
    '<!-- GENERATED FILE -- do not edit by hand. Source of truth is rules.json;',
    '     regenerate via tools/self-improve (self:approve/self:reject/self:sweep). -->',
    '',
    'Rules mined from past session transcripts (see tools/self-improve/README.md) and',
    'accepted in supervised review. Injected into context at session start by',
    '.claude/hooks/inject-rules.sh.',
    '',
  ];
  if (!accepted.length) {
    lines.push('_No accepted rules yet._');
  } else {
    for (const r of accepted) {
      lines.push(`- **[${r.id}]** ${r.text}`);
      if (r.rationale) lines.push(`  - _Why:_ ${r.rationale}`);
    }
  }
  lines.push('');
  return lines.join('\n');
}

function save(repoRoot, store) {
  const { json, md } = storePaths(repoRoot);
  fs.mkdirSync(path.dirname(json), { recursive: true });
  fs.writeFileSync(json, JSON.stringify(store, null, 2) + '\n');
  fs.writeFileSync(md, renderRulesMd(store));
}

module.exports = {
  STATUSES,
  storePaths,
  load,
  save,
  proposeRule,
  transition,
  sweep,
  findById,
  findByNormalizedText,
  renderRulesMd,
  normalizeText,
};
