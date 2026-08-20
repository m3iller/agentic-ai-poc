# Self-improvement pipeline

Meta-tooling for this repo's *own* Claude Code workflow — not part of the Pokemon-catalog
product (`java/`, `dotnet/`). It mines this project's Claude Code session transcripts for
failure patterns and turns them into rules that get injected back into future sessions,
closing the loop described in the two research patterns it implements:

- **ExpeL** (Zhao et al., 2023) — compare high- and low-quality session trajectories to
  extract rules distinguishing them.
- **Reflexion** (Shinn et al., 2023) — turn a session's failure signals (tool errors, retries)
  into a natural-language reflection that becomes a rule for next time.

## Pieces

| Piece | Where |
|---|---|
| Rule store (source of truth) | `memory-bank/rules.json` |
| Accepted-rules render (what gets injected) | `memory-bank/rules.md` (generated — don't hand-edit) |
| Injection hook | `.claude/hooks/inject-rules.sh` (`SessionStart`, see `.claude/settings.json`) |
| Scripts | `tools/self-improve/{lib,bin}` |

## Commands

```
npm run self:review                        # list rules by lifecycle status
npm run self:review -- --status=proposed    # filter
npm run self:review -- --trace <ruleId>     # show a rule's source session(s)
npm run self:stats                          # quality distribution + before/after-acceptance trend
npm run self:extract-insights               # mine new proposed rules from recent sessions
npm run self:extract-insights -- --dry-run  # mine without writing to the store
npm run self:approve -- <ruleId> ["note"]   # proposed -> accepted (supervised)
npm run self:reject -- <ruleId> ["reason"]  # proposed -> archived (supervised)
npm run self:sweep                          # idle accepted/unused rules -> unused -> archived
```

## Rule lifecycle

```
PROPOSED --approve--> ACCEPTED --no hits / 60d--> UNUSED --no hits / 60d more--> ARCHIVED
                          ^                            |
                          `--- pattern re-emerges, reinstate ---'
```

- **Proposed**: mined by `self:extract-insights`, not yet in effect.
- **Accepted**: rendered into `memory-bank/rules.md`, injected into every session via the
  `SessionStart` hook.
- **Unused**: no "hit" (see below) for 60+ days — still in the store, no longer injected.
- **Archived**: unused for another 60+ days — dormant, but not deleted (git history +
  `rules.json` keep it recoverable).
- **Reinstate**: if `self:extract-insights` mines a rule whose text matches an
  unused/archived one, it jumps straight back to accepted instead of re-proposing from
  scratch.

Every lifecycle transition writes `memory-bank/rules.{json,md}` and makes a discrete git
commit (`chore(self-improve): ...`) — nothing else in the working tree is touched or
included, so each transition is independently revertible (`git revert`) and auditable
(`git log --oneline | grep 'chore(self-improve)'`).

## How scoring and extraction actually work (and their limits)

**This is a heuristic approximation, not the ExpeL/Reflexion papers.** Session quality is
scored from the only observable signals Claude Code's transcript format exposes cheaply: tool
`is_error` results and a same-tool-after-error retry heuristic (`lib/scoring.js`). There is no
reliable "did this session actually succeed" signal in the transcript, so error/retry rate is
a proxy, not ground truth — treat proposed rules as suggestions to review, not verified facts.

`self:extract-insights` splits recent sessions into high/low quality groups by that score,
then:
- If the `claude` CLI is on `PATH` (it is, in a normal Claude Code environment), it shells out
  to `claude -p "<prompt>"` with excerpts from both groups and asks for actual ExpeL-style
  comparison + Reflexion-style reflections as JSON. This is a real LLM call — it costs tokens
  and takes ~tens of seconds.
- If `claude` isn't available or the call fails, it falls back to a deterministic template
  ("tool X errored N times, double-check its preconditions") — weaker signal, but keeps the
  command usable with zero dependencies.

**"Hits"** (used to decide idle sweeps): a rule is counted as hit in a later session if that
session's tool errors include the rule's trigger tool (for `trigger.type: "tool_error"`), or
its text mentions the rule's trigger keyword (for `trigger.type: "keyword"`). This tracks
*relevance* (did the rule's concern come up again), not *effectiveness* (did the rule actually
prevent the failure) — the transcript format doesn't give us enough to measure the latter
without a much heavier evaluation harness.

## Reading transcripts

Session data comes from `~/.claude/projects/<slugified-cwd>/*.jsonl` — Claude Code's own,
**undocumented and version-dependent** transcript format. `lib/transcripts.js` parses
defensively (one line at a time, tolerant of unknown record shapes) so a format change
degrades signal quality rather than crashing the pipeline, but don't assume the exact fields
read today (`is_error`, `turn_duration`, etc.) are stable across Claude Code versions.

## Deliberate deviations from the reference slides

- **Injection is `SessionStart`, not `UserPromptSubmit`.** The lifecycle diagram's "injected
  on every prompt" reads as "in force for every prompt in the session," not "re-injected each
  turn" — `SessionStart` gives the same effect for the whole session at a fraction of the
  latency/token cost, the same way `CLAUDE.md` loads once.
- **Sweep commits are batched**, not one commit per rule. "Discrete, revertible commit" is
  read as "separated from unrelated work," not "exactly one rule per commit" — a sweep run is
  one unit of work even if it moves several idle rules at once.
