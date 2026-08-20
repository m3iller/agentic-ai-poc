# Testing Patterns

**Analysis Date:** 2026-08-20

## Test Framework

**Runner:**
- JUnit 5 (Jupiter) via `org.junit.jupiter:junit-jupiter`
- Version: Managed by Spring Boot 3.5.6 BOM
- Config: Configured in `pom.xml` dependency (no separate config file)

**Assertion Library:**
- AssertJ (`org.assertj:assertj-core`) — fluent assertion API
- JUnit built-in assertions NOT used; AssertJ preferred throughout

**Run Commands:**
```bash
cd java && mvn test              # Run all tests
cd java && mvn test -Dtest=PokemonControllerTest  # Run single test class
cd java && mvn test -Dtest=PokemonControllerTest#listWithNoParamsUsesDefaultPage  # Run single test method
cd java && mvn package           # Build + run tests
```

## Test File Organization

**Location:**
- Tests co-located with source in parallel directory structure
- Source: `java/src/main/java/com/ballastlane/agenticai/...`
- Tests: `java/src/test/java/com/ballastlane/agenticai/...` (same package structure)

**Naming:**
- Test class name: Source class name + "Test" suffix
- Examples:
  - `PokemonController.java` → `PokemonControllerTest.java`
  - `PokemonServiceImpl.java` → `PokemonServiceImplTest.java`
  - `PokeApiClientImpl.java` → `PokeApiClientImplTest.java`

**Structure:**
```
java/src/test/java/
└── com/ballastlane/agenticai/
    ├── AppTest.java                                    # Integration test
    └── pokemon/
        ├── controller/
        │   └── PokemonControllerTest.java              # Web layer test
        ├── service/
        │   └── PokemonServiceImplTest.java             # Business logic test
        └── (no DTO tests — records are simple)
    └── pokeapi/
        └── client/
            └── PokeApiClientImplTest.java              # HTTP client test
```

## Test Structure

**Suite Organization:**
All tests follow a consistent pattern with helper methods for test data:

```java
@WebMvcTest(PokemonController.class)
class PokemonControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private PokemonService pokemonService;
    
    // Helper method for test data
    private PokemonPage samplePage(int page, int size) {
        // Create test fixture
    }
    
    @Test
    void descriptiveTestName() throws Exception {
        // Arrange: set up mock behavior
        when(pokemonService.listPokemon(1, 20)).thenReturn(samplePage(1, 20));
        
        // Act: perform request
        mockMvc.perform(get("/api/pokemon"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page").value(1));
        
        // Assert: verify interaction
        verify(pokemonService).listPokemon(1, 20);
    }
}
```

**Patterns:**

1. **Arrange-Act-Assert (AAA):** Tests follow comment structure with clear sections
2. **Setup:** `@BeforeEach` methods initialize fresh objects
3. **Teardown:** Spring framework auto-cleans beans between tests
4. **Assertion:** Multiple assertions in single `andExpect()` chains OR per-field assertions

## Mocking

**Framework:** Mockito (via `org.mockito:mockito-core`)

**Integration:**
- `@ExtendWith(MockitoExtension.class)` for non-Spring unit tests
- `@MockBean` for Spring context tests (auto-wires mock into Spring container)
- `@Mock` for manual mocking in non-Spring tests

**Patterns:**

```java
// Setup mock behavior
when(pokeApiClient.getPokemon("bulbasaur")).thenReturn(bulbasaurDto());

// Parameterized matching
when(pokemonService.listPokemon(anyInt(), anyInt()))
    .thenReturn(samplePage(2, 10));

// Status code matching
when(pokeApiClient.getPokemon("does-not-exist"))
    .thenThrow(new PokeApiNotFoundException("PokeAPI resource not found"));

// Verify interaction
verify(pokemonService).listPokemon(1, 20);
org.mockito.Mockito.verify(pokeApiClient).listPokemon(10, 20);
```

**What to Mock:**
- External dependencies: `PokeApiClient`, `PokemonService` (when testing controller)
- HTTP endpoints: Spring's `MockRestServiceServer` (when testing HTTP client)
- All database/network calls (not yet applicable — no persistence layer yet)

**What NOT to Mock:**
- Record/value objects (used directly, not mocked)
- Domain logic in services (test with real logic, mock only external calls)
- Spring auto-configuration components (use real beans)

## Fixtures and Factories

**Test Data Pattern:**
Each test class contains private helper methods that build test fixtures:

```java
private PokemonPage samplePage(int page, int size) {
    PokemonSummary summary = new PokemonSummary(
        1L, "bulbasaur", "https://example.com/sprite.png", 
        "Seed Pokémon", 69, List.of("overgrow", "chlorophyll"));
    return new PokemonPage(page, size, 1302, List.of(summary));
}

private PokemonDetail sampleDetail() {
    EvolutionStage venusaur = new EvolutionStage("venusaur", List.of());
    EvolutionStage ivysaur = new EvolutionStage("ivysaur", List.of(venusaur));
    EvolutionStage bulbasaur = new EvolutionStage("bulbasaur", List.of(ivysaur));
    return new PokemonDetail(
        1L, "bulbasaur", "https://example.com/artwork.png", 
        "Seed Pokémon", 69, List.of("overgrow", "chlorophyll"),
        List.of(new PokemonStat("hp", 45), new PokemonStat("attack", 49)),
        "A strange seed was planted on its back at birth.",
        bulbasaur);
}

private PokemonDto bulbasaurDto() {
    return new PokemonDto(
        1L, "bulbasaur", 7, 69, 64,
        new PokemonSpritesDto("https://example.com/sprite.png", ...),
        List.of(...), // abilities
        List.of(...), // stats
        List.of(),    // types
        new NamedApiResourceDto("bulbasaur", "..."));
}
```

**Location:**
- Fixtures defined as private methods in test class itself
- No separate factory/builder classes observed
- Test data is inline and localized to the test that uses it
- Shared fixtures (e.g., bulbasaurDto) extracted as separate methods to reduce duplication

## Coverage

**Requirements:** No explicit coverage requirements enforced (not visible in build config)

**View Coverage:**
```bash
# Coverage reports can be added with JaCoCo plugin
# Currently not configured
```

## Test Types

**Unit Tests:**
- **Scope:** Individual service methods
- **Approach:** Mock external dependencies, test business logic directly
- **Example:** `PokemonServiceImplTest` tests each public method with mocked `PokeApiClient`
- **Assertions:** Field-by-field via AssertJ fluent API
- Location: `java/src/test/java/com/ballastlane/agenticai/pokemon/service/PokemonServiceImplTest.java`

**Integration Tests:**
- **Scope:** HTTP layer + Spring context
- **Approach:** Use `@WebMvcTest` for controller testing with mocked services; `@RestClientTest` for HTTP client testing with mocked server
- **Example:** `PokemonControllerTest` tests request routing, parameter validation, and response structure
- Location: `java/src/test/java/com/ballastlane/agenticai/pokemon/controller/PokemonControllerTest.java`

**HTTP Client Tests:**
- **Framework:** Spring's `RestClientTest` with `MockRestServiceServer`
- **Approach:** Mock REST endpoints and verify deserialization
- **Pattern:**
  ```java
  @RestClientTest(PokeApiClientImpl.class)
  class PokeApiClientImplTest {
      @Autowired private PokeApiClientImpl pokeApiClient;
      @Autowired private MockRestServiceServer server;
      
      @Test
      void listPokemonReturnsDeserializedPage() {
          server.expect(requestTo(BASE_URL + "/pokemon?limit=20&offset=0"))
              .andRespond(withSuccess("""{ ... JSON ... }""", MediaType.APPLICATION_JSON));
          
          PokemonListResponseDto result = pokeApiClient.listPokemon(20, 0);
          
          assertThat(result.count()).isEqualTo(1302);
      }
  }
  ```
- Location: `java/src/test/java/com/ballastlane/agenticai/pokeapi/client/PokeApiClientImplTest.java`

**Full Integration Test:**
- **Scope:** Spring Boot application context startup
- **Approach:** `@SpringBootTest` with minimal assertions
- **Example:** `AppTest#contextLoads()` verifies the app starts
- Location: `java/src/test/java/com/ballastlane/agenticai/AppTest.java`

**E2E Tests:**
- Not implemented

## Common Patterns

**Async Testing:**
Not applicable — synchronous REST API. No async/reactive code in current codebase.

**Error Testing:**

```java
// Service layer: Verify exceptions propagate
@Test
void getPokemonDetailPropagatesNotFoundFromClient() {
    when(pokeApiClient.getPokemon("does-not-exist"))
        .thenThrow(new PokeApiNotFoundException("PokeAPI resource not found"));

    assertThatThrownBy(() -> pokemonService.getPokemonDetail("does-not-exist"))
        .isInstanceOf(PokeApiNotFoundException.class);
}

// Controller layer: Verify exception → HTTP status mapping
@Test
void getDetailWhenNotFoundReturnsNotFound() throws Exception {
    when(pokemonService.getPokemonDetail("does-not-exist"))
        .thenThrow(new PokeApiNotFoundException("PokeAPI resource not found"));

    mockMvc.perform(get("/api/pokemon/does-not-exist"))
        .andExpect(status().isNotFound());
}

// HTTP client: Verify status code handling
@Test
void getPokemonThrowsNotFoundOn404() {
    server.expect(requestTo(BASE_URL + "/pokemon/does-not-exist"))
        .andRespond(withStatus(org.springframework.http.HttpStatus.NOT_FOUND));

    assertThatThrownBy(() -> pokeApiClient.getPokemon("does-not-exist"))
        .isInstanceOf(PokeApiNotFoundException.class);
}
```

**Validation Testing:**

```java
// Parameter validation in controller
@Test
void listWithPageLessThanOneReturnsBadRequest() throws Exception {
    mockMvc.perform(get("/api/pokemon").param("page", "0"))
        .andExpect(status().isBadRequest());
}

@Test
void listWithSizeAboveMaxReturnsBadRequest() throws Exception {
    mockMvc.perform(get("/api/pokemon").param("size", "101"))
        .andExpect(status().isBadRequest());
}
```

**JSON Response Validation:**

```java
mockMvc.perform(get("/api/pokemon"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.page").value(1))
    .andExpect(jsonPath("$.size").value(20))
    .andExpect(jsonPath("$.totalCount").value(1302))
    .andExpect(jsonPath("$.items[0].id").value(1))
    .andExpect(jsonPath("$.items[0].name").value("bulbasaur"))
    .andExpect(jsonPath("$.items[0].skills[0]").value("overgrow"))
    .andExpect(jsonPath("$.items[0].skills[1]").value("chlorophyll"));
```

**Test Method Naming:**
All test method names follow descriptive pattern: `[behavior]When[condition][Result]` or `[action]ReturnsExpected[Result]`

Examples:
- `listWithNoParamsUsesDefaultPage()`
- `listWithPageAndSizeParamsPassesThemToService()`
- `getDetailReturnsFullDetailView()`
- `getPokemonThrowsNotFoundOn404()`
- `listWhenPokeApiUnavailableReturnsServiceUnavailable()`

---

*Testing analysis: 2026-08-20*
