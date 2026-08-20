# Codebase Concerns

**Analysis Date:** 2026-08-20

## Tech Debt

### N+1 Query Problem on List Operations

**Issue:** `PokemonServiceImpl.listPokemon()` makes multiple redundant API calls to PokeAPI for each pokemon in the result set.

**Files:** 
- `java/src/main/java/com/ballastlane/agenticai/pokemon/service/PokemonServiceImpl.java` (lines 113-128)
- `dotnet/src/AgenticAi.Api/Pokemon/Services/PokemonService.cs` (same pattern)

**Impact:** 
- For a page of 20 pokemon, the code makes 1 initial list call + 20 `getPokemon()` calls + 20 `getPokemonSpecies()` calls = 41 API calls total
- Linear scaling with page size; requesting 100 pokemon results in 201 API calls
- PokeAPI has rate limiting; this pattern will hit limits immediately in production
- Response times multiply exponentially with page size

**Fix approach:** 
- Implement local persistence (`pokemon-data-synchronization` feature) so most requests don't hit PokeAPI
- Add batch/bulk endpoints to PokeAPI calls if available, or cache the complete pokemon dataset locally
- Until persistence is added, document the performance limitation and reduce default page size from 20 to 5-10 for list operations

### No Caching or Local Persistence

**Issue:** Every request, including repeated requests for the same pokemon, calls PokeAPI. There is no caching layer, no local database, and no persistence layer.

**Files:**
- `java/src/main/java/com/ballastlane/agenticai/pokemon/service/PokemonServiceImpl.java` (constructor comment line 20-21)
- `dotnet/src/AgenticAi.Api/Pokemon/Services/PokemonService.cs` (same pattern)

**Impact:**
- High latency (external API calls on every request)
- Total dependency on PokeAPI availability (no graceful degradation)
- Violates spec requirement for "local relational store" — `specs/delivery-and-evaluation.md` requires a database with Pokemon entity
- No way to support local-only fields (localized names, region metadata, classification tags from `specs/models/pokemon.md`)
- Response-caching feature (`specs/features/response-caching.md`) cannot be implemented without persistence

**Fix approach:**
- Implement `pokemon-data-synchronization` feature (`specs/features/pokemon-data-synchronization.md`) with batch sync of all pokemon from PokeAPI into local relational database
- Add in-memory request cache (e.g., Spring Cache, .NET IMemoryCache) with short TTL (5-10 minutes) for read operations during list/detail fetches
- Implement circuit breaker (Polly in .NET, Resilience4j in Java) to fail gracefully if PokeAPI is unreachable

## Known Bugs

**None currently documented.** Java tests pass (26 tests green). However, test coverage exists only for the implemented features (pokeapi-integration, pokemon-enumeration, pokemon-detail-view). Missing features have no tests yet.

## Security Considerations

### Path Variable Input Validation

**Risk:** The `idOrName` path variable in `GET /api/pokemon/{idOrName}` is passed directly to PokeAPI without validation.

**Files:**
- `java/src/main/java/com/ballastlane/agenticai/pokemon/controller/PokemonController.java` (line 75)
- `dotnet/src/AgenticAi.Api/Pokemon/Controllers/PokemonController.cs` (same pattern)

**Current mitigation:** PokeAPI itself validates the input and returns 404 if invalid. However, once persistence is added, direct database queries using unsanitized user input will be necessary.

**Recommendations:**
- Add input validation decorator/middleware that sanitizes `idOrName` to alphanumeric + hyphen only
- Add length limit (e.g., max 50 characters)
- Log suspicious input patterns for security monitoring

### No Authentication/Authorization

**Risk:** All endpoints are public. Any user can enumerate all pokemon, fetch details, and (once implemented) modify local pokemon records.

**Files:**
- `specs/features/user-authentication.md` — not yet implemented
- No auth middleware in either Java or .NET

**Current mitigation:** This is under development (feature status: Not started). However, the longer auth remains unimplemented, the more code is written assuming public access, making retroactive auth harder.

**Recommendations:**
- Implement `user-authentication` feature before starting `pokemon-local-data-modification`
- Add auth checks in repository layer, not controller (cleaner separation of concerns)
- For list/detail read operations, consider public access; restrict modifications to authenticated users

## Performance Bottlenecks

### External API Latency on Every Request

**Problem:** Every `/api/pokemon` and `/api/pokemon/{idOrName}` request incurs external HTTP latency to PokeAPI (typical 200-500ms per call).

**Files:**
- `java/src/main/java/com/ballastlane/agenticai/pokeapi/client/PokeApiClientImpl.java` (lines 54-89)
- `dotnet/src/AgenticAi.Api/PokeApi/Clients/PokeApiClient.cs` (same pattern)

**Cause:** No local cache, no persistence; all data is fetched on-demand.

**Improvement path:**
1. Phase 1 (immediate): Add in-memory cache with 5-minute TTL
2. Phase 2 (next): Implement pokemon-data-synchronization with nightly batch sync
3. Phase 3 (polish): Add HTTP caching headers (ETag, Cache-Control) to responses for client-side caching

### Evolution Chain Recursive Deserialization

**Problem:** `EvolutionStage` is recursively mapped from `EvolutionChainLinkDto` (lines 94-99 in Java, same pattern in .NET). For deep evolution chains, this involves repeated nested list traversals and object allocations.

**Files:**
- `java/src/main/java/com/ballastlane/agenticai/pokemon/service/PokemonServiceImpl.java` (lines 94-99)

**Cause:** Recursive mapping is correct but not optimized for deep chains.

**Improvement path:**
- Benchmark with Charizard (3 evolutions deep) and deeper chains if they exist in PokeAPI
- If latency is significant, flatten the chain to a list with `parentId` reference instead of nested structure
- Profile memory usage with large chains (Java heap, .NET GC pressure)

## Fragile Areas

### List Enumeration Failure on Any Pokemon Detail Fetch

**Files:**
- `java/src/main/java/com/ballastlane/agenticai/pokemon/service/PokemonServiceImpl.java` (lines 106-110)

**Why fragile:** If any pokemon in a paginated list throws `PokeApiNotFoundException` or `PokeApiUnavailableException` during the detail fetch in `toSummary()`, the entire list request fails with 404 or 503 instead of returning partial results.

**Safe modification:** 
- Wrap individual pokemon fetches in try-catch within `toSummary()`, returning a "summary with missing details" if detail fetch fails
- Add metric/log for partial failures so operators can monitor data completeness
- Decide: should a failed detail fetch skip that pokemon from the list, or include it with nulls?

### Null-Checking Code Paths

**Files:**
- `java/src/main/java/com/ballastlane/agenticai/pokemon/service/PokemonServiceImpl.java` (lines 63-72, 78-88, 130-139)

**Why fragile:** Multiple nested null checks with different behaviors (return null vs. fallback vs. throw). If PokeAPI changes its response shape, multiple code paths could break silently.

**Test coverage:** Limited. Tests cover the happy path (e.g., `getPokemonDetailMapsImageCategoryMassAndSkills()`) and specific fallback cases (e.g., `getPokemonDetailFallsBackToFrontDefaultSpriteWhenNoOfficialArtwork()`), but not all null combinations.

**Safe modification:**
- Use Optional<T> (Java) or nullable properties with explicit null-coalescing (C#) instead of inline null checks
- Add tests for each nested null scenario (null sprites, null official-artwork, null genus, null flavor-text)
- Consider schema validation on DTO deserialization to fail fast if PokeAPI shape changes unexpectedly

## Scaling Limits

### PokeAPI Rate Limiting

**Current capacity:** PokeAPI has undocumented but typical rate limits (100-200 req/min for public API, higher with attribution).

**Limit:** With N+1 queries, a single paginated list request uses up to 41 API calls. At 150 req/min limit, the app can serve only ~3-4 concurrent list requests before throttling.

**Scaling path:**
1. Implement local persistence (eliminates rate limit for cached pokemon)
2. Add client-side exponential backoff + circuit breaker
3. Consider PokeAPI caching proxy or mirror if deploying to production with many users
4. Monitor rate-limit headers (HTTP 429, `X-RateLimit-*`) and queue requests if approaching limit

### PokeAPI Availability Dependency

**Current capacity:** No graceful degradation if PokeAPI is down (all endpoints return 503 Service Unavailable).

**Limit:** Any PokeAPI outage causes complete application unavailability (for read operations).

**Scaling path:**
1. Implement local persistence (read operations can work offline)
2. Add fallback data (e.g., cached JSON snapshot of core pokemon) for display-only mode
3. Return `Retry-After` header with 503 response so clients know to wait

## Dependencies at Risk

### Spring Boot 3.5.6 (Java)

**Risk:** Spring Boot 3.5.6 is relatively recent; no major known vulnerabilities documented. However, Java 21 (the target) reaches end-of-support in September 2026 (6 months away). .NET 10 targets net10.0, which is also recent and has a shorter support window than LTS releases.

**Impact:** 
- Security patches may not be released for older versions after support ends
- Deployment infrastructure (Docker, CI/CD) may drop support for unsupported Java versions

**Migration plan:**
- Plan upgrade to Java 23 or LTS Java 21.0.4+ (long-term support) before September 2026
- Upgrade Spring Boot to 3.6+ if available by then
- For .NET: evaluate moving to .NET 10 LTS or staying on LTS versions (net8.0 or newer LTS) once .NET 10 support windows are clear

### RestClient (Spring 6)

**Risk:** Spring 6's `RestClient` is relatively new (Spring 6.0+) and less battle-tested than `RestTemplate`. No known issues, but fewer real-world deployments.

**Impact:** Unexpected behavior in edge cases (streaming large responses, connection pooling, retry semantics).

**Migration plan:** If issues arise, RestClient code is isolated in `PokeApiClientImpl`; fallback to `RestTemplate` or other HTTP client (OkHttp, HttpClient) is straightforward.

## Missing Critical Features

### Pokemon Data Synchronization

**Problem:** Spec requires "persist Pokemon locally into a relational store" (`specs/delivery-and-evaluation.md`, "Database" section and `specs/features/pokemon-data-synchronization.md`). This is **not implemented**.

**Blocks:** 
- Persistence of proprietary fields (localized names, region, classification tag)
- Caching and performance optimization
- Local data modification feature
- Response caching feature
- Offline/degraded-mode operation

**Priority:** High. Data synchronization is a prerequisite for most remaining features.

### Pokemon Local Data Modification

**Problem:** Feature not started. Requires persistence layer first.

**Blocks:** User Story 04 — allowing system operators to update pokemon records with new proprietary fields.

### User Authentication

**Problem:** Feature not started. All endpoints are public.

**Blocks:** 
- Securing local data modifications
- Tracking user actions
- Future feature: favorited/bookmarked pokemon per user

### Response Caching

**Problem:** Feature not started. Requires persistence layer + auth layer.

**Blocks:** Nice-to-have performance optimization.

### Frontend Client

**Problem:** Feature not started. Spec requires "A frontend that consumes the API" (`specs/delivery-and-evaluation.md`, "Mandatory technical requirements").

**Blocks:** Delivery requirement — submission cannot be evaluated without a working frontend.

## Test Coverage Gaps

### No Integration Tests for Database Operations

**What's not tested:** Once persistence is added, integration tests will be needed for:
- Sync operation success/failure/partial scenarios
- Conflict resolution when PokeAPI data changes
- Transaction boundaries and rollback behavior

**Files:** None yet — repository layer not implemented.

**Risk:** Database bugs will go undetected until production.

**Priority:** High — must add integration tests before shipping persistence layer.

### No Tests for Error Scenarios During List Enumeration

**What's not tested:** If one pokemon's detail fetch fails, does the whole list fail? (Yes, currently.) This is untested.

**Files:** `java/src/test/java/com/ballastlane/agenticai/pokemon/service/PokemonServiceImplTest.java`

**Risk:** Outage of one pokemon's details in PokeAPI breaks list enumeration for all users.

**Priority:** Medium — test and fix partial-failure handling before going to production.

### No Tests for PokeAPI Unavailability / Network Failures

**What's not tested:** Connection timeouts, 503 responses with retry behavior, rate-limit handling.

**Files:** `java/src/test/java/com/ballastlane/agenticai/pokeapi/client/PokeApiClientImplTest.java` — tests only exception mapping, not retry/timeout behavior.

**Risk:** Client code may hang indefinitely or fail in unexpected ways under network stress.

**Priority:** Medium — add timeout + retry tests before production load.

### No Tests for Evolution Chain Cycles

**What's not tested:** PokeAPI's evolution chain structure is acyclic, but if a malformed response contains a cycle, the recursive `toEvolutionStage()` will infinite-loop.

**Files:** `java/src/test/java/com/ballastlane/agenticai/pokemon/service/PokemonServiceImplTest.java` (line 192-209 tests happy path only)

**Risk:** Low (PokeAPI unlikely to return cycles), but impact is severe (stack overflow / OOM).

**Priority:** Low — add a defensive cycle detector or depth limit if building against untrusted data sources.

### No E2E Tests

**What's not tested:** Full HTTP requests from client through controller, service, and back to response.

**Files:** None — controller tests use `@WebMvcTest` (integration test at Spring level, not full HTTP).

**Risk:** Serialization/deserialization mismatches, HTTP status code mapping, or routing errors go undetected until real requests.

**Priority:** Medium — add E2E tests with `MockMvc` or full test server before launch.

### No Tests for Null Fields in DTO Deserialization

**What's not tested:** If PokeAPI response omits a field (e.g., `official_artwork` is null), does the DTO deserialize correctly, or does it fail with `NullPointerException` or mapping error?

**Files:** `java/src/test/java/com/ballastlane/agenticai/pokemon/service/PokemonServiceImplTest.java` (covers some null cases manually, but not via DTO deserialization test)

**Risk:** PokeAPI shape changes → deserialization failures → 502 errors.

**Priority:** Medium — add explicit DTO deserialization tests with null/missing fields.

## Delivery & Documentation Gaps

### Missing README

**Problem:** Spec requires "A comprehensive README: environment setup and technical documentation." (`specs/delivery-and-evaluation.md`). No README exists at `/java/README.md` or root.

**Impact:** Reviewers/users cannot set up the project without significant trial-and-error.

**Fix:** Write README covering:
- Java/Maven setup (Java 21, Maven 3.9+)
- Build commands (`mvn compile`, `mvn test`, `mvn package`)
- Run instructions (how to start the app, what port)
- Environment variables (if any — currently none required)
- Architecture overview (Controller → Service → Client)
- Known limitations (N+1 queries, no persistence)

### Missing Dockerfile

**Problem:** Spec requires "A Dockerfile for containerized execution." (`specs/delivery-and-evaluation.md`). No Dockerfile exists.

**Impact:** Deployment to production/staging requires manual environment setup.

**Fix:** Create `java/Dockerfile`:
```dockerfile
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/agentic-ai-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Missing GenAI Tool Exercise Writeup

**Problem:** Spec requires a separate deliverable demonstrating "GenAI tool fluency" with a task management API scaffold. (`specs/delivery-and-evaluation.md`, "GenAI tools exercise"). No writeup exists.

**Impact:** Evaluation criteria require this; submission is incomplete without it.

**Fix:** Create `GENAI_EXERCISE.md` documenting:
- Prompt used to scaffold the task management API
- Representative output code (key files)
- How AI suggestions were validated/corrected
- Edge cases/auth/validation implemented

### Missing Application Configuration / Seeded Data

**Problem:** Spec requires "The application pre-populated with seeded data or mock credentials for demonstration." No seed data or demo setup exists.

**Impact:** Reviewers must fetch all pokemon from PokeAPI on first run (slow, depends on external service).

**Fix:** 
- Option 1: Add SQL seed script (`seed.sql`) with top 20 pokemon pre-populated (once persistence is added)
- Option 2: Add demo endpoint `/api/pokemon/demo` that returns hardcoded sample data
- Option 3: Document that app auto-seeds on first run by fetching from PokeAPI

---

*Concerns audit: 2026-08-20*
