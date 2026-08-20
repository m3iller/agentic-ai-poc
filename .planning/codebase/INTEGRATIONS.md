# External Integrations

**Analysis Date:** 2026-08-20

## APIs & External Services

**PokéAPI (https://pokeapi.co/api/v2):**
- Service: Public REST API providing comprehensive Pokémon data (stats, species, evolution chains, sprites)
- What it's used for: Fetching Pokémon catalog, individual Pokémon details, species information, evolution relationships
- SDK/Client:
  - Java: Spring's `RestClient` (synchronous HTTP client)
    - Implementation: `java/src/main/java/com/ballastlane/agenticai/pokeapi/client/PokeApiClientImpl.java`
    - Interface: `java/src/main/java/com/ballastlane/agenticai/pokeapi/client/PokeApiClient.java`
  - .NET: `System.Net.Http.HttpClient` (via typed client factory)
    - Implementation: `dotnet/src/AgenticAi.Api/PokeApi/Clients/PokeApiClient.cs`
    - Interface: `dotnet/src/AgenticAi.Api/PokeApi/Clients/IPokeApiClient.cs`
- Auth: None (PokéAPI is publicly accessible, no authentication required)
- Base URL configuration:
  - Java: `pokeapi.base-url` property in `application.yml` (defaults to `https://pokeapi.co/api/v2`)
  - .NET: `PokeApi:BaseUrl` in configuration (defaults to `https://pokeapi.co/api/v2/`)

## Data Storage

**Databases:**
- None currently - both Java and .NET implementations are stateless, pulling data on-demand from PokéAPI
- No database configuration present in pom.xml, .csproj files, or application.yml
- No ORM (JPA/Hibernate, EF Core) dependencies configured

**File Storage:**
- Local filesystem only - no remote file storage or CDN integration
- Static resources served by Spring Boot (Java) or ASP.NET Core (.NET) embedded web servers

**Caching:**
- None configured - all requests to PokéAPI are made on-demand, no response caching layer
- Each client request to `/api/pokemon` results in a fresh HTTP call to PokéAPI

## Authentication & Identity

**Auth Provider:**
- Custom error handling for API failures (no external auth service)
- PokéAPI has no authentication — public access only

**Implementation approach:**
- No user authentication layer present
- No JWT, OAuth2, or session management configured
- Exception handling translates PokéAPI HTTP errors into application-specific exceptions:
  - `PokeApiNotFoundException` (HTTP 404)
  - `PokeApiUnavailableException` (HTTP 5xx or unreachable)
  - `PokeApiClientException` (other HTTP errors)
  - Java handlers: `java/src/main/java/com/ballastlane/agenticai/pokeapi/exception/`
  - .NET handlers: `dotnet/src/AgenticAi.Api/PokeApi/Exceptions/`, routed through `PokeApiExceptionHandler` → RFC 7807 Problem Details

## Monitoring & Observability

**Error Tracking:**
- None detected - no error tracking service (Sentry, Rollbar, etc.) integrated
- Exception handling and HTTP status translation are in place, but no external reporting

**Logs:**
- Default Spring Boot logging (SLF4J with Logback backend)
  - Logs written to console by default in development
  - Configurable via `logback.xml` or Spring Boot properties (not configured, using defaults)
- .NET: Default console logging via `Microsoft.Extensions.Logging`
  - Configured in `Program.cs` (ILogger injected into services)
  - Logs written to console by default

## CI/CD & Deployment

**Hosting:**
- Not deployed - development/local environment only
- No cloud platform (AWS, Azure, GCP) integration detected
- Expected deployment targets per CLAUDE.md:
  - Java: Docker or standalone JAR on any Java 21 runtime
  - .NET: Docker or standalone deployment on any .NET 10 runtime

**CI Pipeline:**
- None configured
- No GitHub Actions, GitLab CI, Jenkins, or similar detected
- Manual `mvn` and `dotnet` commands for local build/test

## Environment Configuration

**Required env vars:**
- None explicitly required
- Optional: `pokeapi.base-url` (Java) or `PokeApi:BaseUrl` (.NET) to override PokéAPI endpoint (rarely needed unless proxying)

**Secrets location:**
- No secrets currently in use (PokéAPI requires no authentication)
- Future authentication layers would store secrets in:
  - Environment variables (local development)
  - `.env` files (Python/Node-style, not currently used)
  - AWS Secrets Manager / Azure Key Vault / HashiCorp Vault (production)
  - Configuration management systems (Spring Cloud Config, etc.)

## Webhooks & Callbacks

**Incoming:**
- None - API only accepts standard HTTP GET/POST requests
- No webhook endpoints for external services to call

**Outgoing:**
- None - API does not push data to external services
- All communication is request-initiated by clients calling `/api/pokemon` endpoints

## Client/API Surface

**REST Endpoints (both Java and .NET mirror this contract):**
- `GET /api/pokemon` - List Pokémon with pagination
  - Query params: `page` (default 1), `size` (default 20, max 100)
  - Returns: `PokemonPageResponse` (paginated list of `PokemonSummaryResponse`)
  - HTTP Status: 200 OK
- `GET /api/pokemon/{idOrName}` - Fetch detailed view of a single Pokémon
  - Path param: `idOrName` (Pokémon name or PokeAPI numeric ID)
  - Returns: `PokemonDetailResponse` (includes stats, description, evolution chain)
  - HTTP Status: 200 OK, 404 Not Found (if Pokémon doesn't exist), 503 Service Unavailable (if PokéAPI unreachable)

**Response Format:**
- JSON via Spring Boot (Java) or ASP.NET Core (.NET)
- JSON naming: Java uses default camelCase (via Jackson defaults); .NET uses snake_case (via `JsonNamingPolicy.SnakeCaseLower`)

---

*Integration audit: 2026-08-20*
