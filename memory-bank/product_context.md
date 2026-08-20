# Product Context

Full detail lives in `specs/features/*.md` and `specs/models/*.md` — this is the "why/what" at a
glance so a new session doesn't have to re-read all eight feature specs to get oriented.

## User stories (US01–US04 numbered in source doc; rest unnumbered)
| # | Feature | Story |
|---|---|---|
| — | [pokeapi-integration](../specs/features/pokeapi-integration.md) | Foundational: talk to the external PokeAPI, map its shape into our domain. No user-facing endpoint. |
| US01 | [pokemon-enumeration](../specs/features/pokemon-enumeration.md) | As a user, browse Pokemon in a paginated list. |
| US02 | [pokemon-detail-view](../specs/features/pokemon-detail-view.md) | As a user, view one Pokemon's full detail: image, core stats, flavor text, evolution chain. 404 if not found. |
| US03 | [pokemon-data-synchronization](../specs/features/pokemon-data-synchronization.md) | As a user, have Pokemon persisted locally with proprietary fields layered on top of raw PokeAPI data. |
| US04 | [pokemon-local-data-modification](../specs/features/pokemon-local-data-modification.md) | As a user, update locally-stored Pokemon with validation. |
| — | [user-authentication](../specs/features/user-authentication.md) | Registration, login, protected vs. public routes. |
| — | [response-caching](../specs/features/response-caching.md) | Nice-to-have: cache service/PokeAPI responses. |
| — | [frontend-client](../specs/features/frontend-client.md) | A frontend consuming the backend API. |

## Models
- **pokemon** ([spec](../specs/models/pokemon.md)) — primary entity; replicates PokeAPI data plus
  proprietary fields added by this app. Needs unique PK + ≥2 descriptive attributes.
- **user** ([spec](../specs/models/user.md)) — secondary collection for auth/user management.

## Domain-mapping decisions made so far (apply to both stacks, see [[progress]])
These were ambiguous in the source doc and got resolved the same way in Java and .NET during
`pokeapi-integration`/`pokemon-enumeration` — treat as settled unless a spec update says
otherwise:
- "category" → PokeAPI's English `genera` (from pokemon-species).
- "skills" → `abilities` names.
- sprite/image → `sprites.front_default`.
- mass → `weight`.
- Invalid/out-of-range `page`/`size` on enumeration → **400**, not clamped to valid range.

## Still open (per spec, unresolved)
- `pokemon-detail-view`: exact itemization of "core statistics" (likely PokeAPI base stats:
  HP/attack/defense/etc., needs confirmation), and whether evolution lineage shows the full chain
  or just next stage.
- Evolution-chain depth in general (flagged since `pokeapi-integration`).

## Out-of-scope reminders
- Editing Pokemon happens only via `pokemon-local-data-modification`, not the detail view.
