#!/usr/bin/env node
'use strict';
// npm run self:review [-- --status=accepted] [-- --trace <ruleId>]
//
// Lists the rule store grouped by lifecycle status, or (with --trace) shows
// an accepted/proposed rule's source sessions and the excerpt that justified
// it.
const path = require('path');
const { repoRoot } = require('../lib/paths');
const ruleStore = require('../lib/ruleStore');
const { parseSession } = require('../lib/transcripts');

function parseArgs(argv) {
  const args = { status: null, trace: null };
  for (let i = 0; i < argv.length; i++) {
    const a = argv[i];
    if (a.startsWith('--status=')) args.status = a.slice('--status='.length);
    else if (a === '--trace') args.trace = argv[++i];
  }
  return args;
}

function main() {
  const root = repoRoot();
  const store = ruleStore.load(root);
  const args = parseArgs(process.argv.slice(2));

  if (args.trace) {
    return trace(root, store, args.trace);
  }

  const byStatus = {};
  for (const s of ruleStore.STATUSES) byStatus[s] = [];
  for (const r of store.rules) (byStatus[r.status] = byStatus[r.status] || []).push(r);

  const statuses = args.status ? [args.status] : ruleStore.STATUSES;
  if (!store.rules.length) {
    console.log('No rules yet. Run `npm run self:extract-insights` to mine some from session history.');
    return;
  }

  for (const status of statuses) {
    const rules = byStatus[status] || [];
    console.log(`\n## ${status.toUpperCase()} (${rules.length})`);
    for (const r of rules) {
      console.log(`- [${r.id}] ${r.text}`);
      console.log(
        `    method=${r.method} hits=${r.hitCount} createdAt=${r.createdAt}${
          r.acceptedAt ? ` acceptedAt=${r.acceptedAt}` : ''
        }`
      );
    }
    if (!rules.length) console.log('  (none)');
  }
  console.log(
    '\nUse `npm run self:approve -- <id>` / `npm run self:reject -- <id>` on proposed rules, ' +
      'or `npm run self:review -- --trace <id>` to see where a rule came from.'
  );
}

function trace(root, store, id) {
  const rule = ruleStore.findById(store, id);
  if (!rule) {
    console.error(`No such rule: ${id}`);
    process.exitCode = 1;
    return;
  }
  console.log(`[${rule.id}] (${rule.status}, method=${rule.method})`);
  console.log(rule.text);
  if (rule.rationale) console.log(`Why: ${rule.rationale}`);
  console.log(`\nHistory:`);
  for (const h of rule.statusHistory) {
    console.log(`  ${h.at}  ${h.from || '(new)'} -> ${h.to}${h.note ? `  (${h.note})` : ''}`);
  }

  const sessionIds = rule.source.sessionIds || [];
  if (!sessionIds.length) {
    console.log('\n(no source sessions recorded)');
    return;
  }
  console.log(`\nSource sessions (${sessionIds.length}):`);
  const { findProjectTranscriptDir } = require('../lib/paths');
  const dir = findProjectTranscriptDir(process.cwd());
  for (const sid of sessionIds) {
    console.log(`\n--- session ${sid} ---`);
    if (!dir) {
      console.log('(transcript directory not found)');
      continue;
    }
    const file = path.join(dir, `${sid}.jsonl`);
    try {
      const session = parseSession(file);
      console.log(`started: ${session.startedAt}  toolUses: ${session.toolUses}  errors: ${session.toolErrors}`);
      if (rule.trigger && rule.trigger.tool && session.errorsByTool[rule.trigger.tool]) {
        console.log(`first ${rule.trigger.tool} error: ${session.errorsByTool[rule.trigger.tool][0].message}`);
      } else if (session.firstUserText) {
        console.log(`opening prompt: ${session.firstUserText.slice(0, 200)}`);
      }
    } catch (err) {
      console.log(`(could not read transcript: ${err.message})`);
    }
  }
}

main();
