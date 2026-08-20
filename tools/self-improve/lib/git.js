'use strict';
// Commits memory-bank/rules.{json,md} as a discrete, revertible change --
// separate from whatever else is in the working tree -- per lifecycle
// transition (propose/accept/reject/reinstate/sweep). Never touches other
// files; never commits if there's nothing staged for the two rule-store
// files (e.g. a sweep run that moved nothing).
const { execFileSync } = require('child_process');
const { storePaths } = require('./ruleStore');

function commitRuleStoreChange(repoRoot, message) {
  const { json, md } = storePaths(repoRoot);
  try {
    execFileSync('git', ['add', '--', json, md], { cwd: repoRoot });
    const diff = execFileSync('git', ['diff', '--cached', '--name-only', '--', json, md], {
      cwd: repoRoot,
      encoding: 'utf8',
    }).trim();
    if (!diff) return { committed: false, reason: 'no changes to rule store' };
    execFileSync('git', ['commit', '-m', message], { cwd: repoRoot });
    return { committed: true };
  } catch (err) {
    return { committed: false, reason: err.message };
  }
}

module.exports = { commitRuleStoreChange };
