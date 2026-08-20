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

## Session Log
*(Newest first. Auto-appended by the Stop hook where possible; add manually otherwise. Keep each
entry to a few lines — link out to specs/STATUS.md or other memory-bank files for detail rather
than duplicating it here.)*

### 2026-08-20 — Memory bank setup
- Created `memory-bank/{project_brief,product_context,tech_context,active_context,progress}.md`,
  seeded from `specs/`, `specs/STATUS.md`, and `CLAUDE.md`.
- Configured a Stop hook (see `.claude/settings.json`) to prompt for/append a session summary to
  this Session Log going forward.
