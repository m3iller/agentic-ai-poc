#!/usr/bin/env node
'use strict';
// npm run self:sweep
//
// Idle-lifecycle pass: accepted rules with no hits in 60+ days -> unused;
// unused rules with no hits for another 60+ days -> archived. Recomputes
// hit counts against session history first. One batched commit per run.
const { repoRoot, findProjectTranscriptDir, listSessionFiles } = require('../lib/paths');
const { parseAllSessions } = require('../lib/transcripts');
const ruleStore = require('../lib/ruleStore');
const { commitRuleStoreChange } = require('../lib/git');

function main() {
  const root = repoRoot();
  const store = ruleStore.load(root);
  const dir = findProjectTranscriptDir(process.cwd());
  const sessions = dir ? parseAllSessions(listSessionFiles(dir)) : [];

  const now = new Date().toISOString();
  const moved = ruleStore.sweep(store, sessions, now);

  if (!moved.length) {
    console.log('[INFO] No idle rules to move.');
    ruleStore.save(root, store); // still persist any hitCount/lastHitAt updates
    return;
  }

  ruleStore.save(root, store);
  for (const { rule, transitionedTo } of moved) {
    console.log(`  [${rule.id}] -> ${transitionedTo}: ${rule.text.slice(0, 70)}`);
  }

  const summary = moved.map((m) => `${m.rule.id}->${m.transitionedTo}`).join(', ');
  const { committed, reason } = commitRuleStoreChange(root, `chore(self-improve): sweep - ${summary}`);
  console.log(committed ? 'Committed.' : `Not committed: ${reason}`);
}

main();
