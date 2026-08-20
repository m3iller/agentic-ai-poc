'use strict';
// Locates repo root and this project's Claude Code transcript directory
// (~/.claude/projects/<slug>/*.jsonl).
const fs = require('fs');
const os = require('os');
const path = require('path');
const { execFileSync } = require('child_process');

function repoRoot() {
  try {
    return execFileSync('git', ['rev-parse', '--show-toplevel'], { encoding: 'utf8' }).trim();
  } catch {
    return process.cwd();
  }
}

function slugify(cwd) {
  // Observed Claude Code convention: path separators become dashes.
  // (Leading "/" -> leading "-".)
  return cwd.replace(/\//g, '-');
}

// Returns the ~/.claude/projects/<slug> directory for `cwd`, falling back to
// scanning all project dirs for one whose transcripts mention this cwd, in
// case the naming convention drifts in a future Claude Code version.
function findProjectTranscriptDir(cwd) {
  const projectsRoot = path.join(os.homedir(), '.claude', 'projects');
  const guess = path.join(projectsRoot, slugify(cwd));
  if (fs.existsSync(guess)) return guess;

  if (!fs.existsSync(projectsRoot)) return null;
  for (const name of fs.readdirSync(projectsRoot)) {
    const dir = path.join(projectsRoot, name);
    let stat;
    try { stat = fs.statSync(dir); } catch { continue; }
    if (!stat.isDirectory()) continue;
    const jsonl = fs.readdirSync(dir).find((f) => f.endsWith('.jsonl'));
    if (!jsonl) continue;
    try {
      const firstLine = fs.readFileSync(path.join(dir, jsonl), 'utf8').split('\n', 1)[0];
      const parsed = JSON.parse(firstLine);
      if (parsed && parsed.cwd === cwd) return dir;
    } catch { /* ignore unreadable/malformed transcript */ }
  }
  return null;
}

function listSessionFiles(transcriptDir) {
  if (!transcriptDir || !fs.existsSync(transcriptDir)) return [];
  return fs
    .readdirSync(transcriptDir)
    .filter((f) => f.endsWith('.jsonl'))
    .map((f) => path.join(transcriptDir, f));
}

module.exports = { repoRoot, slugify, findProjectTranscriptDir, listSessionFiles };
