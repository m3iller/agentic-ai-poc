'use strict';
// Parses a Claude Code session transcript (.jsonl) into a normalized summary
// with the observable failure/effort signals the scoring + extraction steps
// need: tool errors, retry attempts, duration, and a few text excerpts for
// trace/reflection purposes.
//
// NOTE: this reads Claude Code's internal transcript format, which is
// undocumented and may change across versions. Parsing is defensive
// (try/catch per line, tolerant of unknown record shapes) so a format drift
// degrades signal quality rather than crashing the pipeline.
const fs = require('fs');
const path = require('path');

function readJsonlLines(filePath) {
  const raw = fs.readFileSync(filePath, 'utf8');
  const records = [];
  for (const line of raw.split('\n')) {
    if (!line.trim()) continue;
    try {
      records.push(JSON.parse(line));
    } catch {
      // skip malformed line
    }
  }
  return records;
}

function textOf(content) {
  if (typeof content === 'string') return content;
  if (Array.isArray(content)) {
    return content
      .filter((c) => c && c.type === 'text' && typeof c.text === 'string')
      .map((c) => c.text)
      .join('\n');
  }
  return '';
}

function parseSession(filePath) {
  const sessionId = path.basename(filePath, '.jsonl');
  const records = readJsonlLines(filePath);

  let startedAt = null;
  let endedAt = null;
  let toolUses = 0;
  let toolErrors = 0;
  let retries = 0;
  let totalDurationMs = 0;
  let userMessages = 0;
  const errorsByTool = {}; // tool name -> [{ message, timestamp }]
  const toolUseOrder = []; // [{ name, isErrorFollowUp }]
  let firstUserText = '';
  let lastAssistantText = '';

  for (const rec of records) {
    if (rec.timestamp) {
      if (!startedAt) startedAt = rec.timestamp;
      endedAt = rec.timestamp;
    }
    if (rec.isSidechain === true) continue; // subagent work, not the main loop

    if (rec.type === 'system' && rec.subtype === 'turn_duration') {
      totalDurationMs += rec.durationMs || 0;
    }

    if (rec.type === 'user') {
      const content = rec.message && rec.message.content;
      if (Array.isArray(content)) {
        for (const c of content) {
          if (c && c.type === 'tool_result') {
            const name = toolUseOrder.length ? toolUseOrder[toolUseOrder.length - 1].name : 'unknown';
            if (c.is_error) {
              toolErrors += 1;
              (errorsByTool[name] = errorsByTool[name] || []).push({
                message: textOf(c.content).slice(0, 300),
                timestamp: rec.timestamp || null,
              });
              if (toolUseOrder.length) toolUseOrder[toolUseOrder.length - 1].errored = true;
            }
          }
        }
      } else if (typeof content === 'string') {
        userMessages += 1;
        if (!firstUserText) firstUserText = content.slice(0, 500);
      }
      if (rec.message && rec.message.role === 'user' && typeof rec.message.content === 'string') {
        userMessages += 1;
        if (!firstUserText) firstUserText = rec.message.content.slice(0, 500);
      }
    }

    if (rec.type === 'assistant') {
      const content = rec.message && rec.message.content;
      if (Array.isArray(content)) {
        for (const c of content) {
          if (c && c.type === 'tool_use') {
            toolUses += 1;
            // Retry heuristic: this tool was invoked again shortly after one
            // of its prior invocations in this session ended in an error.
            const recentSameTool = toolUseOrder.slice(-3).some((t) => t.name === c.name && t.errored);
            if (recentSameTool) retries += 1;
            toolUseOrder.push({ name: c.name, errored: false });
          }
        }
        const text = textOf(content);
        if (text) lastAssistantText = text.slice(0, 500);
      }
    }
  }

  return {
    sessionId,
    filePath,
    startedAt,
    endedAt,
    toolUses,
    toolErrors,
    retries,
    totalDurationMs,
    userMessages,
    errorsByTool,
    firstUserText,
    lastAssistantText,
  };
}

function parseAllSessions(files) {
  const sessions = [];
  for (const f of files) {
    try {
      sessions.push(parseSession(f));
    } catch {
      // skip unreadable/corrupt transcript
    }
  }
  // Newest first.
  sessions.sort((a, b) => (b.startedAt || '').localeCompare(a.startedAt || ''));
  return sessions;
}

module.exports = { parseSession, parseAllSessions };
