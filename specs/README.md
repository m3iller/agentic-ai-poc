# Specs Index

Extracted from `Java - BLA - Technical Interview Exercise - V2.pdf`. This is a Pokemon-catalog
RESTful API (Clean Architecture, TDD) that integrates with the external
[PokeAPI](https://pokeapi.co/docs/v2), backed by a relational store, plus a frontend client and
basic user authentication.

This spec set is shared by two parallel implementations — Java/Spring Boot under
[`../java`](../java) (`java-agent`) and a .NET 10/ASP.NET Core migration under
[`../dotnet`](../dotnet) (`dotnet-agent`). See [STATUS.md](./STATUS.md) for per-stack progress.

## Features
| Feature | Summary |
|---|---|
| [pokeapi-integration](./features/pokeapi-integration.md) | Foundational integration with the external PokeAPI |
| [pokemon-enumeration](./features/pokemon-enumeration.md) | US01 — paginated Pokemon browsing |
| [pokemon-detail-view](./features/pokemon-detail-view.md) | US02 — full detail view for one Pokemon |
| [pokemon-data-synchronization](./features/pokemon-data-synchronization.md) | US03 — persist Pokemon locally with proprietary fields |
| [pokemon-local-data-modification](./features/pokemon-local-data-modification.md) | US04 — update locally-stored Pokemon with validation |
| [user-authentication](./features/user-authentication.md) | Registration, auth, protected vs. public routes |
| [response-caching](./features/response-caching.md) | Nice to have — cache service/PokeAPI responses |
| [frontend-client](./features/frontend-client.md) | Frontend consuming the backend API |

## Models
| Model | Summary |
|---|---|
| [pokemon](./models/pokemon.md) | Primary entity — replicated PokeAPI data + proprietary fields |
| [user](./models/user.md) | Secondary collection for user management |

## Also see
- [delivery-and-evaluation](./delivery-and-evaluation.md) — architecture constraints, mandatory
  technical requirements, delivery deliverables, the separate GenAI-tools exercise, and
  presentation/code-review evaluation criteria (process requirements, not product features).
