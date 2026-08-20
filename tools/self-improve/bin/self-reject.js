#!/usr/bin/env node
'use strict';
// npm run self:reject -- <ruleId> ["reason"]
//
// Supervised-mode rejection: proposed -> archived directly (never went live,
// so there's nothing to "unuse" first). Commits the rule-store change.
const { repoRoot } = require('../lib/paths');
const ruleStore = require('../lib/ruleStore');
const { commitRuleStoreChange } = require('../lib/git');

function main() {
  const [id, reason] = process.argv.slice(2);
  if (!id) {
    console.error('Usage: npm run self:reject -- <ruleId> ["reason"]');
    process.exitCode = 1;
    return;
  }

  const root = repoRoot();
  const store = ruleStore.load(root);
  const rule = ruleStore.findById(store, id);
  if (!rule) {
    console.error(`No such rule: ${id}`);
    process.exitCode = 1;
    return;
  }
  if (rule.status === 'archived') {
    console.log(`[${id}] already archived.`);
    return;
  }

  const now = new Date().toISOString();
  ruleStore.transition(rule, 'archived', `rejected: ${reason || 'no reason given'}`, now);
  ruleStore.save(root, store);

  const { committed, reason: why } = commitRuleStoreChange(
    root,
    `chore(self-improve): reject rule ${rule.id} - ${rule.text.slice(0, 60)}`
  );
  console.log(`[${rule.id}] rejected. ${committed ? 'Committed.' : `Not committed: ${why}`}`);
}

main();
