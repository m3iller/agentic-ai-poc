#!/usr/bin/env node
'use strict';
// npm run self:stats [-- --sessions=30]
//
// Scores recent sessions, prints the quality distribution, and -- for each
// currently accepted rule -- reports the quality trend before/after its
// acceptance date (the "+14% improvement after acceptance" style signal
// from the demo).
const { repoRoot, findProjectTranscriptDir, listSessionFiles } = require('../lib/paths');
const { parseAllSessions } = require('../lib/transcripts');
const { scoreAll } = require('../lib/scoring');
const ruleStore = require('../lib/ruleStore');

function parseArgs(argv) {
  let sessions = 30;
  for (const a of argv) {
    if (a.startsWith('--sessions=')) sessions = parseInt(a.slice('--sessions='.length), 10) || sessions;
  }
  return { sessions };
}

function avg(nums) {
  if (!nums.length) return null;
  return nums.reduce((a, b) => a + b, 0) / nums.length;
}

function main() {
  const root = repoRoot();
  const { sessions: limit } = parseArgs(process.argv.slice(2));
  const dir = findProjectTranscriptDir(process.cwd());
  if (!dir) {
    console.log('No Claude Code transcript directory found for this project.');
    return;
  }

  const files = listSessionFiles(dir);
  console.log(`[INFO] Loading last ${Math.min(limit, files.length)} of ${files.length} sessions...`);
  const sessions = parseAllSessions(files).slice(0, limit);
  const scored = scoreAll(sessions);

  const byLabel = { high: 0, medium: 0, low: 0 };
  for (const s of scored) byLabel[s.label]++;
  console.log(
    `[DATA] Quality distribution: high=${byLabel.high} medium=${byLabel.medium} low=${byLabel.low} ` +
      `(n=${scored.length})`
  );

  const store = ruleStore.load(root);
  const accepted = store.rules.filter((r) => r.status === 'accepted' && r.acceptedAt);
  if (!accepted.length) {
    console.log('[INFO] No accepted rules yet -- nothing to correlate against.');
    return;
  }

  for (const rule of accepted.sort((a, b) => a.acceptedAt.localeCompare(b.acceptedAt))) {
    const before = scored.filter((s) => s.session.startedAt && s.session.startedAt < rule.acceptedAt);
    const after = scored.filter((s) => s.session.startedAt && s.session.startedAt >= rule.acceptedAt);
    const beforeAvg = avg(before.map((s) => s.score));
    const afterAvg = avg(after.map((s) => s.score));
    if (beforeAvg == null || afterAvg == null) {
      console.log(`[DATA] [${rule.id}] not enough sessions on both sides of acceptance (${rule.acceptedAt}) yet.`);
      continue;
    }
    const delta = beforeAvg === 0 ? null : ((afterAvg - beforeAvg) / beforeAvg) * 100;
    const sign = afterAvg >= beforeAvg ? '+' : '';
    console.log(
      `[DATA] [${rule.id}] "${rule.text.slice(0, 60)}${rule.text.length > 60 ? '…' : ''}": ` +
        `avg ${beforeAvg.toFixed(1)} -> ${afterAvg.toFixed(1)}` +
        (delta != null ? ` (${sign}${delta.toFixed(0)}% quality trend after acceptance)` : '')
    );
  }
}

main();
