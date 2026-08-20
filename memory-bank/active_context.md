# Active Context

*Last updated: 2026-08-20*

## Current focus
Built `tools/self-improve/` — an ExpeL/Reflexion-style self-improvement pipeline over this
project's own Claude Code session history (rule lifecycle, quality scoring, `SessionStart`
injection hook). Not a product feature — same category as the `memory-bank/` setup below: process
tooling for how sessions/subagents work in this repo, not for the Pokemon API itself. See
[[progress]]'s Session Log for the full rundown and `tools/self-improve/README.md` for the design.
No rules have been mined/accepted yet — the pipeline is built and smoke-tested but idle until a
low-quality session (or a manual `self:extract-insights` run) gives it something to propose.

## Where the product work actually stands
Per `specs/STATUS.md`: `pokeapi-integration` and `pokemon-enumeration` (US01) are **Done** in
both `java/` and `dotnet/`, at behavioral parity (see [[tech_context]]). Everything else —
`pokemon-detail-view` (US02), `pokemon-data-synchronization` (US03),
`pokemon-local-data-modification` (US04), `user-authentication`, `response-caching`,
`frontend-client` — is **Not started** in either stack.

## Likely next step (not yet started, not a commitment)
`pokemon-detail-view` (US02) is the natural next feature in spec order for both `java-agent` and
`dotnet-agent`. Two open questions block a clean implementation (see [[product_context]]):
itemization of "core statistics", and evolution-chain depth (full chain vs. next stage only).
Whoever picks this up should either resolve these against the live PokeAPI response shape or
make an explicit call and log it in [[progress]], the way `pokeapi-integration` did for
category/skills/sprite/mass.

## Session continuity convention (how to use this memory bank going forward)
- Read all five `memory-bank/*.md` files, plus recent files under `memory-bank/sessions/`, at
  the start of a session touching this project.
- `specs/` + `specs/STATUS.md` remain the source of truth for *what's built*; this memory bank
  is for *why*, decisions, and cross-session/cross-agent context specs don't capture.
- Each session gets its own file: `memory-bank/sessions/YYYY-MM-DD-<slug>.md`, frontmatter
  `session_id: <id>` + `date: <date>`. A `Stop` hook (`.claude/hooks/memory-bank-stop-check.sh`)
  blocks the session end once if that file is missing, matched by `session_id` — not just "a
  file dated today" — so two sessions on the same day each get recorded, not just the first.
  (Superseded [[progress]]'s old Session Log section on 2026-08-20 — see its Key decisions log.)
- When a decision is made (domain mapping, architecture trade-off, deliberate cross-stack
  divergence), record it in the session file and, if it's durable beyond this session, also add
  it to [[progress]]'s Key decisions log.
- Update this file when the focus shifts to a new feature/task; keep it short — it's "what's
  being worked on right now", not a full history (that's [[progress]] + `memory-bank/sessions/`).
