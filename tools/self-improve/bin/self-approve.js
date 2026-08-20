#!/usr/bin/env node
'use strict';
// npm run self:approve -- <ruleId> ["note"]
//
// Supervised-mode approval: proposed -> accepted. Regenerates rules.md
// (so it's picked up by the SessionStart injection hook next session) and
// commits the rule-store change as a discrete, revertible commit.
const { repoRoot } = require('../lib/paths');
const ruleStore = require('../lib/ruleStore');
const { commitRuleStoreChange } = require('../lib/git');

function main() {
  const [id, note] = process.argv.slice(2);
  if (!id) {
    console.error('Usage: npm run self:approve -- <ruleId> ["note"]');
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
  if (rule.status === 'accepted') {
    console.log(`[${id}] already accepted.`);
    return;
  }

  const now = new Date().toISOString();
  ruleStore.transition(rule, 'accepted', note || 'approved in supervised review', now);
  ruleStore.save(root, store);

  const { committed, reason } = commitRuleStoreChange(
    root,
    `chore(self-improve): accept rule ${rule.id} - ${rule.text.slice(0, 60)}`
  );
  console.log(`[${rule.id}] accepted. ${committed ? 'Committed.' : `Not committed: ${reason}`}`);
}

main();
