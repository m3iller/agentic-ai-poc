# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

A Pokemon-catalog RESTful API being implemented twice, in parallel, under one shared spec set
(`specs/`) — see `specs/README.md`:

- **`java/`** — Java 21 / Spring Boot / Maven implementation (groupId `com.ballastlane`,
  artifactId `agentic-ai`, package root `com.ballastlane.agenticai`). JUnit 5 (Jupiter) for tests.
  Built by `java-agent`.
- **`dotnet/`** — .NET 10 / ASP.NET Core migration target, not yet scaffolded. xUnit for tests,
  EF Core for persistence. Built by `dotnet-agent`.

Both follow the same layered architecture (Controller → Service → Repository) against the same
`specs/`. `specs/STATUS.md` tracks each stack's progress per feature in separate columns — check
it before assuming a feature is or isn't implemented in a given stack.

## Commands

### Java (`java/`)
- `cd java && mvn compile` — compile sources
- `cd java && mvn test` — run all tests
- `cd java && mvn test -Dtest=AppTest` — run a single test class
- `cd java && mvn test -Dtest=AppTest#mainRunsWithoutError` — run a single test method
- `cd java && mvn package` — build the jar (`java/target/agentic-ai-1.0-SNAPSHOT.jar`)

### .NET (`dotnet/`)
Not yet scaffolded — `dotnet-agent`'s first task establishes the solution. Once scaffolded,
expect the usual `dotnet build` / `dotnet test` / `dotnet run` from `dotnet/`.

No linter is configured yet for either stack.

### Self-improvement tooling (`tools/self-improve/`)
Meta-tooling over this repo's *own* Claude Code session history — not part of the product.
Mines past sessions for failure patterns (ExpeL/Reflexion-style) and manages a rule lifecycle
(`memory-bank/rules.json`/`rules.md`) whose accepted rules get injected into context at session
start. See `tools/self-improve/README.md` for the full design and its heuristics/limits.
- `npm run self:review` — list rules by lifecycle status / trace a rule to its source session
- `npm run self:stats` — session quality distribution + before/after-acceptance trend
- `npm run self:extract-insights` — mine new proposed rules from recent sessions
- `npm run self:approve -- <id>` / `npm run self:reject -- <id>` — supervised approval
- `npm run self:sweep` — idle accepted/unused rules → unused → archived
