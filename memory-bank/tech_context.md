# Tech Context

## Stacks
### Java (`java/`)
- Java 21, Spring Boot, Maven.
- groupId `com.ballastlane`, artifactId `agentic-ai`, package root `com.ballastlane.agenticai`.
- Tests: JUnit 5 (Jupiter).
- Built/maintained by the `java-agent` subagent.

Commands:
```
cd java && mvn compile                                   # compile
cd java && mvn test                                      # run all tests
cd java && mvn test -Dtest=AppTest                        # one test class
cd java && mvn test -Dtest=AppTest#mainRunsWithoutError   # one test method
cd java && mvn package                                   # build jar -> java/target/agentic-ai-1.0-SNAPSHOT.jar
```

Notable packages so far: `pokeapi.client` (`PokeApiClient`/`PokeApiClientImpl`, Spring 6
`RestClient`), `pokeapi.dto` (records mirroring raw PokeAPI JSON), `pokeapi.exception`
(NotFound/Unavailable/ClientException), `pokemon.service`, `pokemon.controller`, `pokemon.dto`,
`common.exception` (`GlobalExceptionHandler`, `ErrorResponse`).

### .NET (`dotnet/`)
- .NET 10 / ASP.NET Core. EF Core planned for persistence (not wired yet as of this writing).
- Solution `AgenticAi.sln`, projects `AgenticAi.Api` / `AgenticAi.Api.Tests`, root namespace
  `BallastLane.AgenticAi.Api*`.
- Tests: xUnit; `Moq` for mocking; `Microsoft.AspNetCore.Mvc.Testing` for
  `WebApplicationFactory`-based integration tests.
- Built/maintained by the `dotnet-agent` subagent.

Commands (once scaffolded — it now is):
```
cd dotnet && dotnet build
cd dotnet && dotnet test
cd dotnet && dotnet run --project src/AgenticAi.Api
```

Notable structure so far: `PokeApi/Clients` (`IPokeApiClient`/`PokeApiClient`, typed `HttpClient`
via `AddHttpClient`, base URL from `PokeApi:BaseUrl` config), `PokeApi/Dtos` (System.Text.Json,
`JsonNamingPolicy.SnakeCaseLower` + explicit `[JsonPropertyName]` for kebab-case keys like
`official-artwork`), `PokeApi/Exceptions`, `Pokemon/Services`, `Pokemon/Controllers`,
`Pokemon/Dtos`, `Common/Errors` (`IExceptionHandler` via `AddExceptionHandler`/
`UseExceptionHandler` in `Program.cs`). `Program.cs` exposes `public partial class Program;` so
`WebApplicationFactory<Program>` works from tests.

## Shared architecture (both stacks)
Controller → Service → Repository, Clean Architecture (data-access layer independent from
business-logic layer, independent from API layer). See `.claude/skills/mvc-conventions` for the
Model/View/Controller naming conventions layered on top of this.

## Cross-stack behavioral parity already established
- Same PokeAPI endpoint coverage: list/pokemon/pokemon-species/evolution-chain.
- Same typed-exception shape for upstream failures: 404→NotFound, 5xx/unreachable→Unavailable,
  other 4xx→ClientException; surfaced as 404/503/502 respectively at the controller boundary.
- Java validates page/size explicitly (`@Min`/`@Max(100)` via `spring-boot-starter-validation`);
  .NET gets the equivalent via `[ApiController]`'s automatic model-state validation from
  `[Range]` DataAnnotations — different mechanism, same 400 behavior. This is an expected/known
  divergence point, not a gap.

## No linter configured yet for either stack.

## Agents / tooling in this repo
- `java-agent`, `dotnet-agent` — implement features per stack, TDD-first, update their own
  `specs/STATUS.md` column only.
- `spec-agent` — extracts/maintains specs under `specs/`, checks both STATUS columns before
  rewriting a spec.
- `toolchain-setup` skill — verifies/installs Java 21+Maven / .NET 10 SDK on a new machine.
- `mvc-conventions` skill — Model/View/Controller placement rules shared by both agents.
