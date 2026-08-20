# Codebase Structure

**Analysis Date:** 2026-08-20

## Directory Layout

```
agentic-ai/
├── java/                              # Spring Boot implementation (Java 21)
│   ├── src/main/java/com/ballastlane/agenticai/
│   │   ├── App.java                   # Spring Boot entry point
│   │   ├── common/                    # Cross-cutting utilities
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java
│   │   │       └── ErrorResponse.java
│   │   ├── pokemon/                   # User Story 01-04 (browse, detail, sync, modify)
│   │   │   ├── controller/
│   │   │   │   └── PokemonController.java
│   │   │   ├── service/
│   │   │   │   ├── PokemonService.java
│   │   │   │   ├── PokemonServiceImpl.java
│   │   │   │   ├── PokemonPage.java
│   │   │   │   ├── PokemonSummary.java
│   │   │   │   ├── PokemonDetail.java
│   │   │   │   ├── PokemonStat.java
│   │   │   │   └── EvolutionStage.java
│   │   │   └── dto/
│   │   │       ├── PokemonPageResponse.java
│   │   │       ├── PokemonSummaryResponse.java
│   │   │       ├── PokemonDetailResponse.java
│   │   │       ├── PokemonStatResponse.java
│   │   │       └── EvolutionStageResponse.java
│   │   └── pokeapi/                   # External PokeAPI integration
│   │       ├── client/
│   │       │   ├── PokeApiClient.java
│   │       │   └── PokeApiClientImpl.java
│   │       ├── dto/
│   │       │   ├── PokemonDto.java
│   │       │   ├── PokemonSpeciesDto.java
│   │       │   ├── EvolutionChainDto.java
│   │       │   ├── PokemonListResponseDto.java
│   │       │   ├── NamedApiResourceDto.java
│   │       │   └── (other DTO files)
│   │       └── exception/
│   │           ├── PokeApiException.java (base)
│   │           ├── PokeApiNotFoundException.java
│   │           ├── PokeApiUnavailableException.java
│   │           └── PokeApiClientException.java
│   ├── src/main/resources/
│   │   └── application.yml             # Spring configuration
│   ├── src/test/java/com/ballastlane/agenticai/
│   │   ├── AppTest.java
│   │   ├── pokemon/
│   │   │   ├── controller/
│   │   │   │   └── PokemonControllerTest.java
│   │   │   └── service/
│   │   │       └── PokemonServiceImplTest.java
│   │   └── pokeapi/
│   │       └── client/
│   │           └── PokeApiClientImplTest.java
│   ├── pom.xml                        # Maven configuration
│   └── target/                        # Build artifacts (ignored)
│
├── dotnet/                             # .NET 10 / ASP.NET Core (placeholder)
│   ├── src/AgenticAi.Api/
│   │   ├── Program.cs
│   │   ├── Common/
│   │   ├── Pokemon/
│   │   └── PokeApi/
│   └── tests/AgenticAi.Api.Tests/
│       ├── Pokemon/
│       ├── PokeApi/
│       └── TestSupport/
│
├── specs/                              # Shared feature specifications
│   ├── README.md                       # Index of features and models
│   ├── STATUS.md                       # Per-stack progress tracking
│   ├── features/
│   │   ├── pokeapi-integration.md
│   │   ├── pokemon-enumeration.md
│   │   ├── pokemon-detail-view.md
│   │   ├── pokemon-data-synchronization.md
│   │   ├── pokemon-local-data-modification.md
│   │   ├── user-authentication.md
│   │   ├── response-caching.md
│   │   └── frontend-client.md
│   └── models/
│       ├── pokemon.md
│       └── user.md
│
├── tools/                              # Development and meta-tooling
│   └── self-improve/
│       ├── README.md
│       ├── bin/                        # Scripts
│       ├── lib/                        # Modules
│       └── package.json
│
├── memory-bank/                        # Session continuity and rule management
│   ├── rules.md
│   ├── rules.json
│   └── (session logs)
│
├── .planning/
│   └── codebase/                       # This directory (generated docs)
│       ├── ARCHITECTURE.md
│       ├── STRUCTURE.md
│       ├── CONVENTIONS.md
│       ├── TESTING.md
│       ├── STACK.md
│       ├── INTEGRATIONS.md
│       └── CONCERNS.md
│
├── .claude/                            # Claude Code harness configuration
│   └── settings.json
│
├── CLAUDE.md                           # Project instructions for Claude
├── ONBOARDING.md                       # Developer onboarding guide
└── package.json                        # NPM (for self-improve tooling)
```

## Directory Purposes

**`java/src/main/java/com/ballastlane/agenticai/`:**
- Purpose: Primary Java source code
- Contains: Application classes, controllers, services, clients, DTOs, exception handlers
- Key files: App.java (entry point), PokemonController.java (HTTP handler), PokemonServiceImpl.java (orchestration logic), PokeApiClientImpl.java (external integration)

**`java/src/main/java/com/ballastlane/agenticai/common/`:**
- Purpose: Cross-cutting utilities shared across all modules
- Contains: Exception handling (@RestControllerAdvice, ErrorResponse)
- Key files: GlobalExceptionHandler.java (all exception → HTTP response mapping)

**`java/src/main/java/com/ballastlane/agenticai/pokemon/`:**
- Purpose: User Stories 01-04 (Pokemon browsing, detail view, data sync, local modification)
- Contains: Controller, Service, DTOs, domain model records
- Key files: PokemonController.java (HTTP endpoints), PokemonServiceImpl.java (business logic), PokemonPage/PokemonSummary/PokemonDetail (domain records)

**`java/src/main/java/com/ballastlane/agenticai/pokemon/controller/`:**
- Purpose: HTTP request handling and response mapping
- Contains: @RestController classes mapping to endpoints
- Pattern: No business logic; delegates to services; maps responses via to*Response() helper methods

**`java/src/main/java/com/ballastlane/agenticai/pokemon/service/`:**
- Purpose: Business logic, pagination, data transformation
- Contains: PokemonService (interface), PokemonServiceImpl (implementation), domain records (PokemonPage, PokemonSummary, PokemonDetail, PokemonStat, EvolutionStage)
- Key contract: Depends on PokeApiClient interface; never on implementations; returns immutable domain records to callers

**`java/src/main/java/com/ballastlane/agenticai/pokemon/dto/`:**
- Purpose: HTTP response serialization contracts
- Contains: Response DTOs (PokemonPageResponse, PokemonSummaryResponse, PokemonDetailResponse, etc.)
- Pattern: Jackson-annotated POJOs; created by controller via toResponse() methods; never returned from service

**`java/src/main/java/com/ballastlane/agenticai/pokeapi/`:**
- Purpose: External PokeAPI integration
- Contains: Client interface/implementation, external DTOs, exception hierarchy
- Key contract: PokeApiClient interface abstracts HTTP transport; implementations translate HTTP errors to domain exceptions

**`java/src/main/java/com/ballastlane/agenticai/pokeapi/client/`:**
- Purpose: External API transport
- Contains: PokeApiClient (interface), PokeApiClientImpl (Spring RestClient-backed)
- Pattern: Catches HTTP errors and translates to domain exceptions

**`java/src/main/java/com/ballastlane/agenticai/pokeapi/dto/`:**
- Purpose: Deserialization of PokeAPI JSON responses
- Contains: DTOs mirroring PokeAPI schema (PokemonDto, PokemonSpeciesDto, EvolutionChainDto, etc.)
- Pattern: Jackson-annotated POJOs with `fail-on-unknown-properties: false` (see application.yml) to tolerate upstream schema drift

**`java/src/main/java/com/ballastlane/agenticai/pokeapi/exception/`:**
- Purpose: Domain exception hierarchy for external failures
- Contains: PokeApiException (base), PokeApiNotFoundException, PokeApiUnavailableException, PokeApiClientException
- Pattern: Thrown by client; caught by GlobalExceptionHandler for HTTP translation

**`java/src/main/resources/`:**
- Purpose: Non-Java application configuration
- Contains: application.yml (Spring configuration, PokeAPI base URL)
- Key config: Jackson `fail-on-unknown-properties: false` allows PokeAPI schema evolution

**`java/src/test/java/com/ballastlane/agenticai/`:**
- Purpose: Unit and integration tests
- Contains: AppTest.java, controller/service/client tests using JUnit 5 + Mockito + Spring Test
- Pattern: Mirror src/main structure; tests use @WebMvcTest/@SpringBootTest; mock external dependencies

**`specs/`:**
- Purpose: Shared specification set for both Java and .NET implementations
- Contains: Features (requirements documents), models (entity specs), STATUS.md (per-stack progress)
- Key files: README.md (index), STATUS.md (tracks which features are implemented in which stack), features/*.md (acceptance criteria and acceptance scenarios)

**`specs/features/`:**
- Purpose: User story and feature definitions
- Contains: pokeapi-integration.md (US00), pokemon-enumeration.md (US01), pokemon-detail-view.md (US02), pokemon-data-synchronization.md (US03), etc.
- Usage: Both java-agent and dotnet-agent use these specs to implement features and verify correctness

**`specs/models/`:**
- Purpose: Entity/data model specifications
- Contains: pokemon.md (fields, relationships, constraints), user.md (authentication entity)
- Usage: Schema validation, API contract definition

**`tools/self-improve/`:**
- Purpose: Meta-tooling for mining failure patterns from session history and managing rule lifecycle
- Contains: ExpeL/Reflexion-style analyzer, rule DB, approved rules injected into session context
- Key files: README.md, package.json (npm scripts: `npm run self:extract-insights`, `npm run self:approve`)

**`memory-bank/`:**
- Purpose: Session continuity and learning rule management
- Contains: rules.md (human-readable), rules.json (machine-readable), session logs
- Usage: Injected into context at session start; accumulated from failed executions and supervised approval

**`.planning/codebase/`:**
- Purpose: Generated codebase analysis documents (this directory)
- Contains: ARCHITECTURE.md, STRUCTURE.md, CONVENTIONS.md, TESTING.md, STACK.md, INTEGRATIONS.md, CONCERNS.md
- Usage: Consumed by gsd-plan-phase, gsd-execute-phase, and other analysis tools

**`.claude/`:**
- Purpose: Claude Code harness configuration
- Contains: settings.json (permissions, hooks, model settings)
- Usage: Project-specific preferences, automation triggers, permission allowlists

## Key File Locations

**Entry Points:**
- Java: `java/src/main/java/com/ballastlane/agenticai/App.java` — Spring Boot @SpringBootApplication, runs on port 8080
- HTTP API: `java/src/main/java/com/ballastlane/agenticai/pokemon/controller/PokemonController.java` — @RestController at /api/pokemon

**Configuration:**
- Spring: `java/src/main/resources/application.yml` — Spring autoconfiguration, PokeAPI base URL
- Project: `CLAUDE.md` — Claude Code instructions, build/test commands
- Development: `ONBOARDING.md` — Developer onboarding guide

**Core Logic:**
- Service interface: `java/src/main/java/com/ballastlane/agenticai/pokemon/service/PokemonService.java`
- Service implementation: `java/src/main/java/com/ballastlane/agenticai/pokemon/service/PokemonServiceImpl.java`
- External client: `java/src/main/java/com/ballastlane/agenticai/pokeapi/client/PokeApiClientImpl.java`
- Error handling: `java/src/main/java/com/ballastlane/agenticai/common/exception/GlobalExceptionHandler.java`

**Testing:**
- Controller tests: `java/src/test/java/com/ballastlane/agenticai/pokemon/controller/PokemonControllerTest.java`
- Service tests: `java/src/test/java/com/ballastlane/agenticai/pokemon/service/PokemonServiceImplTest.java`
- Client tests: `java/src/test/java/com/ballastlane/agenticai/pokeapi/client/PokeApiClientImplTest.java`

**Specifications:**
- Spec index: `specs/README.md`
- Progress tracking: `specs/STATUS.md` (per-stack feature completion)
- Feature specs: `specs/features/` (user stories with acceptance criteria)
- Data models: `specs/models/` (Pokemon, User entity definitions)

## Naming Conventions

**Files:**
- Controllers: `{Entity}Controller.java` — e.g., PokemonController.java
- Services: `{Entity}Service.java` (interface), `{Entity}ServiceImpl.java` (implementation) — e.g., PokemonService.java, PokemonServiceImpl.java
- DTOs: `{Entity}{Purpose}Response.java` (HTTP response) or `{Purpose}Dto.java` (external) — e.g., PokemonDetailResponse.java, PokemonDto.java
- Exceptions: `{Context}Exception.java` or `{Context}{Type}Exception.java` — e.g., PokeApiNotFoundException.java
- Records: `{Entity}.java` (domain model) — e.g., PokemonPage.java, PokemonDetail.java
- Tests: `{Class}Test.java` — e.g., PokemonControllerTest.java

**Directories:**
- Package structure mirrors directory layout: `com.ballastlane.agenticai.pokemon.controller` → `java/src/main/java/com/ballastlane/agenticai/pokemon/controller/`
- Feature modules named by domain: `pokemon`, `pokeapi`
- Cross-cutting utilities: `common`

**Classes:**
- Controllers: `@RestController`, endpoint names are plural or resource names (PokemonController)
- Services: Interface (PokemonService), Implementation (PokemonServiceImpl)
- DTOs: Suffix Response (HTTP response) or Dto (external), annotated for Jackson
- Records: Named for domain entity (PokemonPage, PokemonSummary, PokemonDetail)
- Exceptions: Suffix Exception (PokeApiNotFoundException, PokeApiUnavailableException)

**Methods:**
- Controllers: HTTP verb + entity (list(), getDetail(), create())
- Services: Business operation name (listPokemon(), getPokemonDetail())
- Mapping helpers: toResponse() (overloaded) to convert between layers
- Getters: Direct field access on records (page(), size(), items())

## Where to Add New Code

**New Feature (e.g., Pokemon modification):**
- Primary code: `java/src/main/java/com/ballastlane/agenticai/pokemon/service/` (PokemonService interface extension, PokemonServiceImpl implementation)
- Controller extension: `java/src/main/java/com/ballastlane/agenticai/pokemon/controller/PokemonController.java` (new @PostMapping/@PutMapping method)
- DTOs: `java/src/main/java/com/ballastlane/agenticai/pokemon/dto/` (new request/response DTOs)
- Tests: `java/src/test/java/com/ballastlane/agenticai/pokemon/` (controller test, service test)
- Spec: `specs/features/{feature-name}.md` (before implementation; shared with dotnet-agent)

**New External Integration (e.g., authentication service):**
- Client interface: `java/src/main/java/com/ballastlane/agenticai/{domain}/client/{Service}Client.java`
- Client impl: `java/src/main/java/com/ballastlane/agenticai/{domain}/client/{Service}ClientImpl.java`
- DTOs: `java/src/main/java/com/ballastlane/agenticai/{domain}/dto/` (external response DTOs)
- Exceptions: `java/src/main/java/com/ballastlane/agenticai/{domain}/exception/` (domain-specific exception types)
- Tests: `java/src/test/java/com/ballastlane/agenticai/{domain}/client/`

**New Component/Module:**
- Create package under `java/src/main/java/com/ballastlane/agenticai/{feature}/`
- Subdivide into: `controller/`, `service/`, `dto/`, `exception/` (as needed)
- Mirror tests under `java/src/test/java/com/ballastlane/agenticai/{feature}/`
- Document in `specs/` before implementation

**Utilities (cross-cutting):**
- Shared helpers: `java/src/main/java/com/ballastlane/agenticai/common/` (e.g., ErrorResponse, validation utilities)
- Avoid: Placing feature-specific code in `common/`; use feature packages instead

**Configuration:**
- Application properties: `java/src/main/resources/application.yml` or application-{profile}.yml for profiles
- Spring beans (if manual registration needed): `java/src/main/java/com/ballastlane/agenticai/{feature}/config/` (follow naming: `{Feature}Config.java`)

## Special Directories

**`java/target/`:**
- Purpose: Maven build artifacts
- Generated: Yes
- Committed: No (in .gitignore)
- Contents: Compiled classes, packaged JAR, test reports, dependencies

**`dotnet/*/bin/` and `dotnet/*/obj/`:**
- Purpose: .NET build artifacts
- Generated: Yes
- Committed: No
- Contents: Compiled assemblies, intermediate build files

**`.git/`:**
- Purpose: Version control metadata
- Generated: Yes (by git)
- Committed: No (dotfiles)

**`memory-bank/`:**
- Purpose: Learning rule database and session logs
- Generated: Yes (by self-improve tooling)
- Committed: Yes (rules.md, rules.json accumulated over sessions)
- Usage: Injected into context at session start

**`.planning/codebase/`:**
- Purpose: Generated codebase analysis documents
- Generated: Yes (by gsd-map-codebase)
- Committed: Yes (should be checked in for team reference)

---

*Structure analysis: 2026-08-20*
