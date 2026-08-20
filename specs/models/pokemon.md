# Model: Pokemon

## Description
The primary domain entity. Represents a Pokemon replicated locally from PokeAPI, extended with
proprietary fields not present upstream. Source doc requires "a primary entity ... records must
include a unique primary key and a minimum of two descriptive attributes."

## Fields

### Replicated from PokeAPI (read-mostly, populated via sync)
| Field | Type | Required | Notes |
|---|---|---|---|
| id | identifier (PK) | yes | Local unique primary key |
| pokeApiId | identifier | yes | Reference to the source PokeAPI resource |
| name | string | yes | |
| sprite | URL/string | yes | Image shown in list view |
| image | URL/string | yes | Larger image shown in detail view (may be same as sprite — open question) |
| category | string | yes | Exact PokeAPI field it maps to is an open question (see [pokemon-enumeration](../features/pokemon-enumeration.md)) |
| mass | number | yes | |
| skills | collection of string | yes | Abilities; shown in list view |
| stats | collection (name/value) | yes | Core statistics; shown in detail view |
| description | string | yes | Narrative/flavor-text description |
| evolutionChain | structured/collection | yes | Evolutionary lineage; shown in detail view |

### Proprietary (local-only, added per User Story 03)
| Field | Type | Required | Notes |
|---|---|---|---|
| localizedName | string | no | "Localized nomenclature" |
| region | string | no | "Geographical metadata" |
| classificationTag | string | no | "Internal classification tags" |

## Relationships
- None to other local entities specified in the source doc. (A future association to `User`,
  e.g. "favorited by," is not mentioned and is out of scope unless requested.)

## Used by
- [pokeapi-integration](../features/pokeapi-integration.md)
- [pokemon-enumeration](../features/pokemon-enumeration.md)
- [pokemon-detail-view](../features/pokemon-detail-view.md)
- [pokemon-data-synchronization](../features/pokemon-data-synchronization.md)
- [pokemon-local-data-modification](../features/pokemon-local-data-modification.md)
- [response-caching](../features/response-caching.md)
- [frontend-client](../features/frontend-client.md)

## Open questions
- The exact set of "proprietary fields" is illustrative in the source doc ("use cases include...")
  rather than an exhaustive list — more may be needed.
- Whether `sprite` and `image` are genuinely distinct fields or the same value used in two
  contexts is unclear from the source doc.
