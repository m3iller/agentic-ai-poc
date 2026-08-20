#!/usr/bin/env bash
# Stop hook: enforce per-session continuity files under memory-bank/sessions/.
#
# Before Claude finishes a session in this repo, make sure a file exists
# under memory-bank/sessions/ whose frontmatter carries THIS session's
# session_id. Checking session_id (not just "a file dated today exists")
# means multiple sessions on the same day each get their own recorded file
# instead of the second one silently passing the gate for free. If missing,
# block the stop once and tell Claude what to write. If it has already been
# blocked once this stop cycle (stop_hook_active), let it go instead of
# looping forever.
set -euo pipefail

input="$(cat)"
stop_hook_active="$(printf '%s' "$input" | jq -r '.stop_hook_active // false' 2>/dev/null || echo false)"

if [ "$stop_hook_active" = "true" ]; then
  exit 0
fi

repo_root="${CLAUDE_PROJECT_DIR:-$(git rev-parse --show-toplevel 2>/dev/null || pwd)}"
memory_bank_dir="$repo_root/memory-bank"
sessions_dir="$memory_bank_dir/sessions"

# No memory bank in this repo (or it was removed) -> nothing to enforce.
if [ ! -d "$memory_bank_dir" ]; then
  exit 0
fi

session_id="$(printf '%s' "$input" | jq -r '.session_id // empty' 2>/dev/null || true)"
today="$(date +%Y-%m-%d)"

mkdir -p "$sessions_dir"

if [ -n "$session_id" ] && grep -rl "^session_id: ${session_id}$" "$sessions_dir" >/dev/null 2>&1; then
  exit 0
fi

reason=$(cat <<EOF
No memory-bank/sessions/ file found for this session (session_id: ${session_id:-unknown}).

Before finishing, create memory-bank/sessions/${today}-<short-slug>.md (slug = a few
kebab-case words describing the focus of this session) with this frontmatter and structure:

---
session_id: ${session_id:-unknown}
date: ${today}
---

# <Session Title>

## Concepts / topics covered
- ...

## What changed
- Files/specs touched, decisions made (domain mapping, architecture trade-offs, deliberate
  cross-stack divergences). Durable decisions also belong in the Key decisions log in
  memory-bank/progress.md -- this per-session file is the narrative, that log is the index.

## Follow-ups
- Anything left open for a future session.

Also update memory-bank/active_context.md "Current focus" if it shifted during this session.
Then stop again.
EOF
)

jq -n --arg reason "$reason" '{decision: "block", reason: $reason}'
exit 0
