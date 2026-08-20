<!-- refreshed: 2026-08-20 -->
# Architecture

**Analysis Date:** 2026-08-20

## System Overview

This is a Pokemon-catalog RESTful API implemented in two parallel stacks (Java/Spring Boot and .NET/ASP.NET Core) against shared specifications. The architecture follows layered separation: HTTP handlers (Controllers) → business logic (Services) → external client integration (PokeAPI Client) → data serialization (DTOs).

The current implementation focuses on browsing and detail views of Pokemon data sourced entirely from the external PokeAPI, with no local persistence yet (see feature backlog in `specs/`).

```text
┌──────────────────────────────────────────────────────────────┐
│         HTTP Layer: REST Controllers                          │
│         `java/.../pokemon/controller/PokemonController`       │
│         Endpoints: GET /api/pokemon, GET /api/pokemon/{id}   │
└────────────────────┬─────────────────────────────────────────┘
                     │ (Model objects: PokemonPage, PokemonDetail)
                     ▼
┌──────────────────────────────────────────────────────────────┐
│         Service Layer: Business Logic                         │
│         `java/.../pokemon/service/PokemonService(Impl)`      │
│         - Fetches via PokeAPI Client                         │
│         - Transforms DTOs to domain objects                   │
│         - Pagination, filtering, data enrichment             │
└────────────────────┬─────────────────────────────────────────┘
                     │ (Model objects: PokemonSummary, PokemonDetail)
                     ▼
┌──────────────────────────────────────────────────────────────┐
│    Integration Layer: External Client + Exception Handling   │
│    `java/.../pokeapi/client/PokeApiClient(Impl)`             │
│    Translates HTTP errors to domain exceptions               │
│    PokeApiNotFoundException, PokeApiUnavailableException     │
└────────────────────┬─────────────────────────────────────────┘
                     │ (HTTP requests via Spring RestClient)
                     ▼
┌──────────────────────────────────────────────────────────────┐
│         External API: PokeAPI (https://pokeapi.co/api/v2)   │
│         Resources: /pokemon, /pokemon-species, /evolution    │
└──────────────────────────────────────────────────────────────┘
```

## Component Responsibilities

| Component | Responsibility | File |
|-----------|----------------|------|
| PokemonController | Maps HTTP requests/responses; delegates to service | `java/src/main/java/com/ballastlane/agenticai/pokemon/controller/PokemonController.java` |
| PokemonService | Interface defining business operations | `java/src/main/java/com/ballastlane/agenticai/pokemon/service/PokemonService.java` |
| PokemonServiceImpl | Implements service contract; combines PokeAPI resources into domain objects | `java/src/main/java/com/ballastlane/agenticai/pokemon/service/PokemonServiceImpl.java` |
| PokeApiClient | Interface defining PokeAPI transport contract | `java/src/main/java/com/ballastlane/agenticai/pokeapi/client/PokeApiClient.java` |
| PokeApiClientImpl | Implements client; uses Spring RestClient to fetch data and translate HTTP errors | `java/src/main/java/com/ballastlane/agenticai/pokeapi/client/PokeApiClientImpl.java` |
| GlobalExceptionHandler | Cross-cutting exception mapping to HTTP status codes | `java/src/main/java/com/ballastlane/agenticai/common/exception/GlobalExceptionHandler.java` |

## Pattern Overview

**Overall:** Layered architecture with interface-based dependency injection, strict separation of concerns (HTTP handlers do not contain business logic; services do not know about HTTP).

**Key Characteristics:**
- Controllers translate HTTP requests into service method calls and service responses into JSON
- Services depend on abstractions (PokeApiClient interface), not implementations
- No repository layer yet — services call the external client directly per request
- All exceptions translated at the boundary (PokeApiClientImpl catches HTTP errors; GlobalExceptionHandler maps to HTTP responses)
- Domain objects (PokemonPage, PokemonSummary, PokemonDetail) are immutable records, never DTOs

## Layers

**Controller Layer:**
- Purpose: HTTP request/response handling, parameter validation, DTO conversion
- Location: `java/src/main/java/com/ballastlane/agenticai/pokemon/controller/`
- Contains: REST endpoints (@GetMapping, @PostMapping), parameter binding, response marshalling
- Depends on: PokemonService (interface), domain model records (PokemonPage, PokemonDetail), DTOs (PokemonPageResponse, PokemonDetailResponse)
- Used by: Spring DispatcherServlet, HTTP clients

**Service Layer:**
- Purpose: Business rules, data transformation, orchestration of calls to external integrations
- Location: `java/src/main/java/com/ballastlane/agenticai/pokemon/service/`
- Contains: PokemonService interface, PokemonServiceImpl (orchestrates PokeAPI calls, maps DTOs to domain records)
- Depends on: PokeApiClient (interface), domain model records (PokemonSummary, PokemonDetail, PokemonStat, EvolutionStage)
- Used by: Controllers

**Integration Layer (PokeAPI Client):**
- Purpose: External API access, HTTP error translation to domain exceptions
- Location: `java/src/main/java/com/ballastlane/agenticai/pokeapi/client/`
- Contains: PokeApiClient interface, PokeApiClientImpl (Spring RestClient-backed HTTP calls)
- Depends on: PokeAPI DTOs (PokemonDto, PokemonSpeciesDto, EvolutionChainDto), exception types
- Used by: Services

**Exception Handling:**
- Purpose: Translate domain/integration exceptions to HTTP responses
- Location: `java/src/main/java/com/ballastlane/agenticai/common/exception/`
- Contains: GlobalExceptionHandler (@RestControllerAdvice), ErrorResponse DTO, exception hierarchy
- Depends on: All exception types (PokeApiNotFoundException, PokeApiUnavailableException, PokeApiClientException)
- Used by: Spring exception resolution mechanism

**DTO Layer (Serialization Boundaries):**
- Purpose: JSON marshalling/unmarshalling at API boundary (request/response) and external integration (PokeAPI)
- Location: `java/src/main/java/com/ballastlane/agenticai/pokemon/dto/` (response DTOs), `java/src/main/java/com/ballastlane/agenticai/pokeapi/dto/` (external DTOs)
- Contains: POJOs annotated for Jackson serialization (no business logic)
- Depends on: Nothing (pure data carriers)
- Used by: Controllers (response DTOs), PokeApiClientImpl (external DTOs)

## Data Flow

### Primary Request Path: List Pokemon (US01)

1. HTTP GET `/api/pokemon?page=1&size=20` arrives at `PokemonController.list()` (line 49-55)
2. Controller calls `PokemonService.listPokemon(1, 20)` passing page and size
3. Service (`PokemonServiceImpl.listPokemon`, line 102-111) calculates offset and calls `PokeApiClient.listPokemon(size, offset)`
4. Client (`PokeApiClientImpl.listPokemon`, line 35-37) makes GET request to `/pokemon?limit={size}&offset={offset}` on PokeAPI
5. PokeAPI returns `PokemonListResponseDto` with paginated results (name + resource URL)
6. Service iterates results, calling `PokeApiClient.getPokemon()` and `PokeApiClient.getPokemonSpecies()` for each entry
7. Service combines data into `PokemonSummary` records (sprite, category, abilities, weight)
8. Service returns `PokemonPage` record containing list of summaries
9. Controller transforms `PokemonPage` to `PokemonPageResponse` via `toResponse()` mapping (line 57-62)
10. Spring serializes response to JSON and sends HTTP 200

**State Management:**
- No mutable state — all domain objects are immutable records
- No caching — every service call fans out to PokeAPI in real-time
- No session state — stateless request handling

### Secondary Request Path: Get Pokemon Detail (US02)

1. HTTP GET `/api/pokemon/bulbasaur` (or `/api/pokemon/1`) arrives at `PokemonController.getDetail()` (line 74-78)
2. Controller calls `PokemonService.getPokemonDetail("bulbasaur")`
3. Service (`PokemonServiceImpl.getPokemonDetail`, line 35-61) calls `PokeApiClient.getPokemon()` to fetch core data
4. Service calls `PokeApiClient.getPokemonSpecies()` to fetch species metadata (genus, description, evolution chain reference)
5. Service calls `PokeApiClient.getEvolutionChain()` with the URL from species metadata
6. Service transforms DTOs to domain objects: extracts English descriptions/genera, normalizes flavor text, builds evolution chain tree recursively
7. Service returns `PokemonDetail` record containing full data
8. Controller transforms `PokemonDetail` to `PokemonDetailResponse` via `toResponse()` mapping (line 80-94)
9. If Pokemon not found, `PokeApiClientImpl` throws `PokeApiNotFoundException`; `GlobalExceptionHandler.handleNotFound()` catches it, returns HTTP 404
10. Spring serializes response to JSON and sends HTTP 200

### Error Paths

**Not Found (HTTP 404):**
- PokeAPI returns 404 → `PokeApiClientImpl.getRelative()` catches `HttpClientErrorException.NotFound` (line 60-61) → throws `PokeApiNotFoundException`
- `GlobalExceptionHandler.handleNotFound()` (line 23-26) catches it → returns HTTP 404 with ErrorResponse body

**Service Unavailable (HTTP 503):**
- PokeAPI returns 5xx → `PokeApiClientImpl` catches `HttpServerErrorException` (line 62-63) → throws `PokeApiUnavailableException`
- `GlobalExceptionHandler.handleUnavailable()` (line 28-31) → returns HTTP 503 with ErrorResponse body

**Bad Gateway (HTTP 502):**
- PokeAPI returns 4xx (other than 404) → `PokeApiClientImpl` catches `HttpStatusCodeException` (line 64-66) → throws `PokeApiClientException`
- `GlobalExceptionHandler.handleClientException()` (line 33-36) → returns HTTP 502 with ErrorResponse body

**Connection Failure (HTTP 503):**
- Network unreachable, timeout → `PokeApiClientImpl` catches `ResourceAccessException` (line 67-68) → throws `PokeApiUnavailableException`
- `GlobalExceptionHandler.handleUnavailable()` → returns HTTP 503 with ErrorResponse body

**Bad Request Parameters (HTTP 400):**
- Page/size parameters fail Jakarta validation (@Min, @Max annotations on `PokemonController.list()` line 51-52)
- Spring's validation layer catches `ConstraintViolationException` (line 38-41)
- `GlobalExceptionHandler.handleInvalidRequestParameters()` → returns HTTP 400 with ErrorResponse body

## Key Abstractions

**PokeApiClient Interface:**
- Purpose: Seam for external API integration; enables mocking in tests, transport swapping in future
- Examples: `java/src/main/java/com/ballastlane/agenticai/pokeapi/client/PokeApiClient.java` (interface), `PokeApiClientImpl.java` (implementation)
- Pattern: Dependency injection via constructor (line 30 in PokeApiClientImpl); all callers depend on interface, not implementation

**Domain Records:**
- Purpose: Immutable, type-safe representations of business entities (never to be confused with DTOs)
- Examples: `PokemonPage`, `PokemonSummary`, `PokemonDetail`, `PokemonStat`, `EvolutionStage` in `java/src/main/java/com/ballastlane/agenticai/pokemon/service/`
- Pattern: Java records (immutable by language feature); used only within service layer and returned to controller; never serialized directly

**Response DTOs:**
- Purpose: JSON marshalling contract at HTTP boundary
- Examples: `PokemonPageResponse`, `PokemonSummaryResponse`, `PokemonDetailResponse`, `EvolutionStageResponse` in `java/src/main/java/com/ballastlane/agenticai/pokemon/dto/`
- Pattern: Controller explicitly maps domain records to DTOs (never returns domain objects to client)

**PokeAPI DTOs:**
- Purpose: Deserialization of external API responses
- Examples: `PokemonDto`, `PokemonSpeciesDto`, `EvolutionChainDto` in `java/src/main/java/com/ballastlane/agenticai/pokeapi/dto/`
- Pattern: Jackson-annotated POJOs; service layer consumes these and transforms to domain records

## Entry Points

**Spring Boot Application:**
- Location: `java/src/main/java/com/ballastlane/agenticai/App.java`
- Triggers: `java -jar` or IDE run; runs Spring Boot autoconfiguration, instantiates beans, starts embedded Tomcat on port 8080
- Responsibilities: Component scanning, dependency injection, exception handler registration

**PokemonController (HTTP Entry):**
- Location: `java/src/main/java/com/ballastlane/agenticai/pokemon/controller/PokemonController.java`
- Triggers: HTTP GET requests to `/api/pokemon` or `/api/pokemon/{idOrName}`
- Responsibilities: Request routing, parameter extraction, response conversion, input validation

## Architectural Constraints

- **Layering:** No cross-layer shortcuts (e.g., controller directly calling PokeAPI client). Services are the only business entry point. DTOs live at boundaries only.
- **Dependencies:** Services depend on PokeApiClient interface, never on the implementation. Interfaces enable testability and future transport swapping.
- **Threading:** Single-threaded event loop (Spring's default). No explicit concurrency control; PokeAPI calls are synchronous.
- **Global state:** None. PokeApiClientImpl's RestClient is created once per app lifetime (via Spring singleton) but is thread-safe and stateless.
- **Immutability:** Domain records are immutable by design (Java record semantics). Service layer never mutates objects; it creates new ones.
- **Null handling:** Explicit null checks in service layer (e.g., `species.flavorTextEntries() == null` line 79 in PokemonServiceImpl); null values bubble up as `null` fields in domain objects (e.g., `detail.image()` can be null).

## Anti-Patterns

### Calling External API from Controller

**What happens:** If a controller directly called `PokeApiClient.getPokemon()`, it would bypass PokemonService.
**Why it's wrong:** Controllers become responsible for business logic (orchestration, pagination, transformation). Hard to test without HTTP mocks. Services become thin and logic fragments across layers.
**Do this instead:** All external calls go through PokemonService. Controllers call services only. See `PokemonController.list()` → `PokemonService.listPokemon()` → `PokeApiClient.listPokemon()`.

### Returning Raw PokeAPI DTOs from Controller

**What happens:** If `PokemonController.getDetail()` returned `PokemonDto` directly, the JSON response would expose PokeAPI's schema.
**Why it's wrong:** API contract becomes dependent on upstream schema changes. Clients see unnecessary fields. No control over response shape.
**Do this instead:** Service transforms PokeAPI DTOs to domain records; controller maps records to response DTOs. E.g., `PokemonDto` → `PokemonDetail` → `PokemonDetailResponse`. See `PokemonServiceImpl.getPokemonDetail()` and `PokemonController.toResponse(PokemonDetail)`.

### Throwing HTTP Exceptions from Service

**What happens:** If `PokemonServiceImpl.getPokemonDetail()` threw a Spring HTTP exception (e.g., `HttpClientErrorException`), the exception type would leak integration details.
**Why it's wrong:** Service becomes tightly coupled to HTTP transport. Changes to error transport (e.g., switching to gRPC) require service refactoring. Services should not know HTTP exists.
**Do this instead:** Service throws domain exceptions (PokeApiNotFoundException). GlobalExceptionHandler translates domain exceptions to HTTP. See `PokeApiClientImpl` catching HTTP errors and throwing `PokeApiNotFoundException`, and `GlobalExceptionHandler.handleNotFound()` translating it to 404.

## Error Handling

**Strategy:** Layered exception translation — HTTP errors caught at client boundary, translated to domain exceptions, then mapped to HTTP responses at controller boundary.

**Patterns:**
- PokeApiClientImpl catches Spring HTTP exceptions (HttpClientErrorException, HttpServerErrorException, ResourceAccessException) and throws domain exceptions (PokeApiNotFoundException, PokeApiUnavailableException, PokeApiClientException)
- GlobalExceptionHandler catches domain exceptions and returns HTTP responses with ErrorResponse DTOs
- Controller parameter validation (@Min, @Max, @Validated) triggers constraint violation exceptions, caught by GlobalExceptionHandler and returned as HTTP 400

## Cross-Cutting Concerns

**Logging:** Not yet implemented. Spring Boot actuator is not configured. Consider adding before moving to production.

**Validation:** Request parameters validated via Jakarta annotations (@Min, @Max) on controller method parameters. PokeAPI responses assumed valid (deserialization errors would bubble as 500).

**Authentication:** Not yet implemented. See `specs/features/user-authentication.md` for planned approach.

---

*Architecture analysis: 2026-08-20*
