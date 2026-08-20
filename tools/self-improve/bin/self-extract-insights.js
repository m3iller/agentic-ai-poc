#!/usr/bin/env node
'use strict';
// npm run self:extract-insights [-- --sessions=30] [-- --dry-run]
//
// The ExpeL + Reflexion mining step:
//  1. Score recent sessions, split into high/low quality groups.
//  2. Ask `claude -p` (if available) to compare the two groups (ExpeL) and
//     turn the low-quality group's failure signals into candidate rules
//     (Reflexion), returned as strict JSON.
//  3. Fall back to a deterministic per-tool-error template if the `claude`
//     CLI isn't on PATH or the call fails -- lower quality, but keeps the
//     pipeline usable without an extra dependency.
//  4. Propose the surviving (deduped) rules into the rule store and commit.
const { execFileSync } = require('child_process');
const { repoRoot, findProjectTranscriptDir, listSessionFiles } = require('../lib/paths');
const { parseAllSessions } = require('../lib/transcripts');
const { scoreAll } = require('../lib/scoring');
const ruleStore = require('../lib/ruleStore');
const { commitRuleStoreChange } = require('../lib/git');

function parseArgs(argv) {
  let sessions = 30;
  let dryRun = false;
  for (const a of argv) {
    if (a.startsWith('--sessions=')) sessions = parseInt(a.slice('--sessions='.length), 10) || sessions;
    if (a === '--dry-run') dryRun = true;
  }
  return { sessions, dryRun };
}

function summarizeForPrompt(scored, label) {
  return scored
    .map(({ session, score }) => {
      const errors = Object.entries(session.errorsByTool)
        .map(([tool, errs]) => `${tool} x${errs.length} (e.g. "${errs[0].message.slice(0, 120)}")`)
        .join('; ');
      return (
        `session ${session.sessionId} [${label}, score=${score}]\n` +
        `  opening prompt: ${session.firstUserText.slice(0, 150)}\n` +
        `  toolUses=${session.toolUses} errors=${session.toolErrors} retries=${session.retries}\n` +
        (errors ? `  errors: ${errors}\n` : '')
      );
    })
    .join('\n');
}

function buildPrompt(high, low) {
  return `You are analyzing coding-agent session transcripts from one software project, using two
research patterns:

- ExpeL (Zhao et al., 2023): compare high-quality and low-quality trajectories to
  algorithmically extract rules distinguishing them.
- Reflexion (Shinn et al., 2023): turn failure signals (tool errors, retries) from
  low-quality sessions into natural-language reflections that become preventive rules.

HIGH-QUALITY SESSIONS (clean, few/no tool errors or retries):
${summarizeForPrompt(high, 'high') || '(none available)'}

LOW-QUALITY SESSIONS (tool errors and/or retry loops):
${summarizeForPrompt(low, 'low') || '(none available)'}

Propose at most 5 new operating rules a coding agent working in this repo should follow to
avoid the failure patterns above, informed by what the high-quality sessions did differently
where that's visible. Each rule must be concrete and actionable (not "be careful"), phrased as
a short imperative instruction.

Respond with ONLY a JSON array (no prose, no markdown fences), where each element is:
{
  "text": "<the rule, imperative, one sentence>",
  "rationale": "<one sentence: why, referencing the observed pattern>",
  "method": "expel" | "reflexion",
  "trigger": { "type": "tool_error", "tool": "<tool name that errored, or null>" }
}
If you cannot find a real, well-supported pattern, return [].`;
}

function callClaude(prompt) {
  const out = execFileSync('claude', ['-p', prompt], {
    encoding: 'utf8',
    timeout: 90000,
    maxBuffer: 10 * 1024 * 1024,
  });
  const match = out.match(/\[[\s\S]*\]/);
  if (!match) throw new Error('no JSON array found in claude output');
  const parsed = JSON.parse(match[0]);
  if (!Array.isArray(parsed)) throw new Error('claude output was not a JSON array');
  return parsed;
}

function templateFallback(low) {
  // Deterministic, no LLM: one candidate rule per tool that errored more
  // than once across the low-quality group.
  const counts = {};
  for (const { session } of low) {
    for (const [tool, errs] of Object.entries(session.errorsByTool)) {
      counts[tool] = (counts[tool] || 0) + errs.length;
    }
  }
  return Object.entries(counts)
    .filter(([, n]) => n > 1)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 5)
    .map(([tool, n]) => ({
      text: `Before relying on ${tool}, double-check its preconditions -- it errored ${n} time(s) across recent low-quality sessions.`,
      rationale: `${tool} accounted for ${n} tool errors in sessions with low quality scores (heuristic fallback, no LLM available).`,
      method: 'reflexion',
      trigger: { type: 'tool_error', tool },
    }));
}

function main() {
  const root = repoRoot();
  const { sessions: limit, dryRun } = parseArgs(process.argv.slice(2));
  const dir = findProjectTranscriptDir(process.cwd());
  if (!dir) {
    console.log('No Claude Code transcript directory found for this project. Nothing to extract from.');
    return;
  }

  const files = listSessionFiles(dir);
  const sessions = parseAllSessions(files).slice(0, limit);
  const scored = scoreAll(sessions);
  const high = scored.filter((s) => s.label === 'high');
  const low = scored.filter((s) => s.label === 'low');
  console.log(`[INFO] ${scored.length} sessions scored: ${high.length} high, ${low.length} low quality.`);

  if (!low.length) {
    console.log('[INFO] No low-quality sessions found -- nothing to mine reflections from.');
    return;
  }

  let candidates;
  let usedClaude = false;
  try {
    execFileSync('which', ['claude']);
    candidates = callClaude(buildPrompt(high, low));
    usedClaude = true;
  } catch (err) {
    console.log(`[WARN] claude CLI unavailable/failed (${err.message}); using template fallback.`);
    candidates = templateFallback(low);
  }
  console.log(`[INFO] ${candidates.length} candidate rule(s) generated (${usedClaude ? 'claude -p' : 'template'}).`);

  if (!candidates.length) return;

  const store = ruleStore.load(root);
  const now = new Date().toISOString();
  const sessionIds = low.map((s) => s.session.sessionId);
  const results = [];
  for (const c of candidates) {
    if (!c || typeof c.text !== 'string' || !c.text.trim()) continue;
    const { rule, action } = ruleStore.proposeRule(
      store,
      {
        text: c.text.trim(),
        rationale: c.rationale || null,
        method: c.method === 'expel' || c.method === 'reflexion' ? c.method : 'reflexion',
        trigger: c.trigger || { type: 'manual', tool: null, pattern: null },
        sessionIds,
      },
      now
    );
    results.push({ rule, action });
  }

  for (const { rule, action } of results) {
    console.log(`  [${action}] [${rule.id}] ${rule.text}`);
  }

  if (dryRun) {
    console.log('[INFO] --dry-run: not writing to rule store.');
    return;
  }

  ruleStore.save(root, store);
  const newCount = results.filter((r) => r.action === 'proposed').length;
  const reinstatedCount = results.filter((r) => r.action === 'reinstated').length;
  const parts = [];
  if (newCount) parts.push(`propose ${newCount} rule(s)`);
  if (reinstatedCount) parts.push(`reinstate ${reinstatedCount} rule(s)`);
  if (parts.length) {
    const { committed, reason } = commitRuleStoreChange(root, `chore(self-improve): ${parts.join(', ')}`);
    if (!committed) console.log(`[WARN] not committed: ${reason}`);
  }

  console.log('\nRun `npm run self:review` to inspect, then `npm run self:approve -- <id>` to accept.');
}

main();
