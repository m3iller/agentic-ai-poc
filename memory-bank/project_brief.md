# Project Brief

## What this is
A Pokemon-catalog RESTful API, implemented **twice in parallel** under one shared spec set
(`specs/`), as an interview/technical exercise (source: "Java - BLA - Technical Interview
Exercise - V2.pdf").

- `java/` — Java 21 / Spring Boot / Maven. Built by `java-agent`.
- `dotnet/` — .NET 10 / ASP.NET Core migration target. Built by `dotnet-agent`.

Both follow the same layered architecture (Controller → Service → Repository) against the same
`specs/`. Neither stack is "the real one" — they're a deliberate parallel-migration exercise, so
divergences in approach (not just tech) are expected and tracked, not bugs.

## Goals / scope
- Integrate with the external [PokeAPI](https://pokeapi.co/docs/v2) for Pokemon data.
- Persist Pokemon locally (relational store) with proprietary fields layered on top of the raw
  PokeAPI data.
- Support basic user authentication (registration, protected vs. public routes).
- Ship a frontend client that consumes the backend API.
- Nice to have: response caching layer; any functionality beyond spec.

## Architecture constraints (apply to both stacks)
- **Clean Architecture**: separate data-access layer (persistence) from business-logic layer
  (domain rules/validation), independent from the API layer.
- **TDD preferred**; thorough unit test coverage required for every core component.
- Relational DB (or equivalent): primary entity `pokemon`, secondary collection `user`; every
  record needs a unique PK + at least two descriptive attributes.
- Consistent REST semantics: standard HTTP verbs, required params, consistent return shapes
  across CRUD endpoints.

## Mandatory deliverables
- Public Git repo, tests, proper error handling, a frontend consuming the API.
- Comprehensive README (env setup + technical docs).
- Seeded data / mock credentials for demo.
- A Dockerfile for containerized execution.
- Separate write-up: GenAI-tools-fluency exercise (a small task-management CRUD API, own
  language choice) — prompt used, representative output, validation/correction notes. Not part
  of the Pokemon app itself.

## Evaluation criteria (live panel presentation)
Clean Architecture adherence, test coverage/TDD, code quality, bug-free functionality, clarity of
presentation, GenAI tool fluency/critical thinking.

## Non-goals / open questions
- No explicit deadline is stated in the source doc.
- Domain-mapping ambiguities (e.g. what PokeAPI's "category"/"skills"/"core statistics" map to,
  evolution-chain depth) are deliberately deferred past `pokeapi-integration` — see
  [[active_context]] and `specs/STATUS.md` for where each stack landed on them.

## Source of truth
`specs/README.md` indexes all feature and model specs — always check `specs/STATUS.md` before
assuming a feature is/isn't implemented in a given stack. This memory bank is a *supplement* for
context specs don't capture (decisions, rationale, session continuity), not a replacement for
`specs/`.
