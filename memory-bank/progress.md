# Progress

High-level status + a running decisions/session log. `specs/STATUS.md` remains the authoritative
per-stack feature ledger (updated by `java-agent`/`dotnet-agent` themselves) — this file adds the
narrative/decision trail around it, and is where session summaries get appended.

## Status snapshot (mirrors `specs/STATUS.md` — check there for full detail/notes)
| Feature | Java | .NET |
|---|---|---|
| pokeapi-integration | Done | Done |
| pokemon-enumeration (US01) | Done | Done |
| pokemon-detail-view (US02) | Not started | Not started |
| pokemon-data-synchronization (US03) | Not started | Not started |
| pokemon-local-data-modification (US04) | Not started | Not started |
| user-authentication | Not started | Not started |
| response-caching | Not started | Not started |
| frontend-client | Not started | Not started |

## Key decisions log
- **Domain mapping** (pokeapi-integration/pokemon-enumeration, both stacks): category→`genera`
  (English), skills→`abilities` names, sprite→`sprites.front_default`, mass→`weight`. Applied
  identically in Java and .NET.
- **Validation semantics** (pokemon-enumeration, both stacks): invalid/out-of-range `page`/`size`
  → 400, not clamped. Java: explicit `@Min`/`@Max(100)`. .NET: `[Range]` DataAnnotations +
  `[ApiController]` automatic model-state validation — different mechanism, same outcome; treated
  as an acceptable stack-idiomatic divergence, not a gap to close.
- **Error-shape parity** (pokeapi-integration, both stacks): upstream PokeAPI failures normalized
  to NotFound/Unavailable/ClientException, surfaced as 404/503/502 at the controller boundary in
  both stacks.
- **Deliberately deferred**: exact PokeAPI "category"/"skills"/"core statistics" mapping and
  evolution-chain depth were left as open questions during `pokeapi-integration` rather than
  guessed at — category/skills/sprite/mass got resolved during `pokemon-enumeration`; "core
  statistics" and evolution-chain depth remain open for `pokemon-detail-view`.
- **2026-08-20**: introduced `memory-bank/` (this file set) + a Stop hook to auto-append session
  summaries here, so decisions and session continuity survive across sessions/subagents without
  relying on `specs/` (which intentionally stays product-scoped, not process/decision-scoped).
- **2026-08-20**: replaced the Session Log below with one file per session under
  `memory-bank/sessions/` (`YYYY-MM-DD-<slug>.md`, frontmatter carries `session_id`). The old
  date-only check (`grep "^### {today}"` in `progress.md`) let a second same-day session pass the
  Stop-hook gate for free without ever being recorded — checking `session_id` against the
  `sessions/` directory closes that gap. This log (`## Key decisions log`) stays the durable
  cross-session index; `sessions/*.md` holds the per-session narrative.
- **2026-08-20**: added `tools/self-improve/` — a meta-tooling pipeline (ExpeL/Reflexion-style)
  that mines this project's own Claude Code session transcripts for failure patterns and
  proposes rules through a lifecycle (proposed→accepted→unused→archived) in
  `memory-bank/rules.json`/`rules.md`. Accepted rules inject into context via a new
  `SessionStart` hook (`.claude/hooks/inject-rules.sh`), not `UserPromptSubmit` — same
  once-per-session load as `CLAUDE.md`, avoids re-injection cost every turn. Quality scoring is
  an explicit heuristic (tool-error/retry rate — no real exit-criteria signal exists in the
  transcript format); extraction shells out to `claude -p` for real ExpeL/Reflexion generation
  when available, with a deterministic template fallback. Every lifecycle transition is its own
  git commit (`chore(self-improve): ...`). See `tools/self-improve/README.md` for the full
  design and documented limits.

## Session Log (historical — frozen 2026-08-20)
*Superseded by `memory-bank/sessions/` (one file per session, gated by the Stop hook on
`session_id` — see the 2026-08-20 decision above). The entries below predate that change and are
kept as history; no new entries get appended here going forward.*

### 2026-08-20 — Self-improvement pipeline (`tools/self-improve/`)
- Built the ExpeL/Reflexion-style self-improvement loop requested from a set of reference
  slides: `tools/self-improve/{lib,bin}` (Node, zero deps), a rule store
  (`memory-bank/rules.{json,md}`), and a `SessionStart` hook (`.claude/hooks/inject-rules.sh`)
  that injects accepted rules into context.
- npm scripts: `self:review`, `self:stats`, `self:extract-insights`, `self:approve`,
  `self:reject`, `self:sweep` (root `package.json`, new — first Node tooling in this repo).
- Key decisions (also logged above): SessionStart not UserPromptSubmit for injection; hits
  track relevance not effectiveness; sweep commits batched per run, not per rule. Smoke-tested
  against this project's real transcripts (`~/.claude/projects/...`) — 5 sessions, all scored
  "high" quality, so extraction correctly found nothing to mine yet (no fabricated demo data
  seeded into the store).
- Not yet done: no rules have been proposed/accepted for real — that happens naturally once a
  low-quality session shows up, or someone runs `self:extract-insights` after one.

### 2026-08-20 — Memory bank setup
- Created `memory-bank/{project_brief,product_context,tech_context,active_context,progress}.md`,
  seeded from `specs/`, `specs/STATUS.md`, and `CLAUDE.md`.
- Configured a Stop hook (see `.claude/settings.json`) to prompt for/append a session summary to
  this Session Log going forward.
