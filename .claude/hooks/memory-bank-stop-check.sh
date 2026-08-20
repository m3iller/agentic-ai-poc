#!/usr/bin/env bash
# Stop hook: enforce session-summary continuity for memory-bank/.
#
# Before Claude finishes a session in this repo, make sure
# memory-bank/progress.md's "## Session Log" got a new entry for today. If
# not, block the stop once and tell Claude what to write. If it has already
# been blocked once this stop cycle (stop_hook_active), let it go instead of
# looping forever.
set -euo pipefail

input="$(cat)"
stop_hook_active="$(printf '%s' "$input" | jq -r '.stop_hook_active // false' 2>/dev/null || echo false)"

if [ "$stop_hook_active" = "true" ]; then
  exit 0
fi

repo_root="${CLAUDE_PROJECT_DIR:-$(git rev-parse --show-toplevel 2>/dev/null || pwd)}"
progress_file="$repo_root/memory-bank/progress.md"

# No memory bank in this repo (or it was removed) -> nothing to enforce.
if [ ! -f "$progress_file" ]; then
  exit 0
fi

today="$(date +%Y-%m-%d)"

if grep -q "^### ${today}" "$progress_file"; then
  exit 0
fi

reason=$(cat <<EOF
memory-bank/progress.md Session Log has no entry for today (${today}) yet.

Before finishing, append a short entry at the TOP of the "## Session Log"
section in memory-bank/progress.md:

### ${today} -- short title
- What changed (link the specs/files touched)
- Any decisions made (domain mapping, architecture trade-offs, deliberate
  cross-stack divergences) -- add these to the "Key decisions log" section
  too if they are durable, not just this-session trivia

Also update memory-bank/active_context.md "Current focus" if it shifted
during this session. Then stop again.
EOF
)

jq -n --arg reason "$reason" '{decision: "block", reason: $reason}'
exit 0
