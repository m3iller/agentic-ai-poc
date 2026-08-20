#!/usr/bin/env bash
# SessionStart hook: inject memory-bank/rules.md (the ACCEPTED rules only)
# into context, once per session -- same lifecycle stage as CLAUDE.md.
#
# Deliberately SessionStart, not UserPromptSubmit: accepted rules apply to
# every prompt for the rest of the session once loaded, so re-injecting on
# every single turn would just burn tokens/latency for no additional signal.
set -euo pipefail

repo_root="${CLAUDE_PROJECT_DIR:-$(git rev-parse --show-toplevel 2>/dev/null || pwd)}"
rules_file="$repo_root/memory-bank/rules.md"

if [ ! -f "$rules_file" ]; then
  exit 0
fi

# Nothing accepted yet -> nothing worth injecting.
if ! grep -q '^\- \*\*\[' "$rules_file"; then
  exit 0
fi

content="$(cat "$rules_file")"
jq -n --arg content "$content" \
  '{hookSpecificOutput: {hookEventName: "SessionStart", additionalContext: $content}}'
