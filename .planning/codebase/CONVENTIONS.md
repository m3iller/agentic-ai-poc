# Coding Conventions

**Analysis Date:** 2026-08-20

## Naming Patterns

**Files:**
- Class/Interface files: PascalCase (e.g., `PokemonController.java`, `PokemonService.java`)
- Record files: PascalCase (e.g., `PokemonDetail.java`, `PokemonSummary.java`)
- DTO files: PascalCase with "Response" or "Dto" suffix (e.g., `PokemonDetailResponse.java`, `PokemonDto.java`)
- Test files: Same name as class under test with "Test" suffix (e.g., `PokemonControllerTest.java`)
- Exception files: PascalCase with "Exception" suffix (e.g., `PokeApiNotFoundException.java`)

**Classes and Interfaces:**
- PascalCase for all class and interface names
- Controllers end in "Controller" (e.g., `PokemonController`)
- Services end in "Service" or "ServiceImpl" (interfaces: `PokemonService`, implementations: `PokemonServiceImpl`)
- DTOs end in "Response" (outbound), "Dto" (inbound/internal), or "Request" (e.g., `PokemonDetailResponse.java`)
- Exceptions end in "Exception" (e.g., `PokeApiNotFoundException.java`)
- Records use simple domain names (e.g., `PokemonDetail`, `PokemonSummary`)

**Methods:**
- camelCase for all method names
- Getter methods: Simple property access via records or direct method names (e.g., `pokemon.name()`, `pokemon.id()`)
- Helper/converter methods: Verb-based names (e.g., `toResponse()`, `toSummary()`, `englishGenus()`, `image()`, `toStat()`)
- Filter/search methods: `filter...`, `find...`, etc.
- Void methods: Action verbs (e.g., `validate()`, `save()`, `delete()`)

**Variables:**
- camelCase for local variables and fields (e.g., `pokemonService`, `result`, `mockMvc`)
- Record field names: camelCase (e.g., `id`, `name`, `category`, `evolutionChain`)
- Constructor parameters: camelCase matching field names where applicable (e.g., `PokemonDto dto`)

**Constants:**
- UPPER_SNAKE_CASE for all static final constants (e.g., `DEFAULT_PAGE`, `MAX_SIZE`, `ENGLISH_LANGUAGE`, `BASE_URL`)
- Constants declared in class scope or at method scope

**Type Names:**
- PascalCase for class/interface names
- Generic type parameters: Single uppercase letters (e.g., `T`, `K`, `V`) or descriptive PascalCase
- Enum names: PascalCase (enum type), UPPER_SNAKE_CASE (enum constants)

## Code Style

**Formatting:**
- No explicit linter configured (noted in `CLAUDE.md`)
- Standard Java conventions apply:
  - 4-space indentation
  - Opening braces on same line (Java style)
  - Single blank line between methods
  - Imports organized by package (java/javax/org/com)
  - No wildcard imports

**Linting:**
- No linter currently configured (as noted in `CLAUDE.md` — "No linter is configured yet for either stack")
- Future addition recommended for consistent formatting

**Record Usage:**
- Records used for immutable value objects without getters/setters
- Record fields accessed directly as methods: `record.fieldName()`
- No JavaBean-style getters on records

## Import Organization

**Order (observed pattern):**
1. Standard Java packages (`java.*`, `javax.*`)
2. Jakarta packages (validation, etc.)
3. Spring packages (`org.springframework.*`)
4. Third-party packages (`org.mockito.*`, `org.junit.*`)
5. Project packages (`com.ballastlane.agenticai.*`)

**Path Aliases:**
- No custom path aliases used in project
- Fully qualified imports used throughout

**Static Imports:**
- Mockito methods: `import static org.mockito.Mockito.*`
- MockMvc methods: `import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*`
- Spring test result matchers: `import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*`
- AssertJ methods: `import static org.assertj.core.api.Assertions.*`

## Error Handling

**Pattern: Centralized Exception Handler**
- All exceptions from services/clients are propagated up to `GlobalExceptionHandler` (`com.ballastlane.agenticai.common.exception.GlobalExceptionHandler`)
- Controllers do NOT catch exceptions — they propagate as business exceptions
- Specific domain exceptions mapped to HTTP status codes:
  - `PokeApiNotFoundException` → HTTP 404
  - `PokeApiUnavailableException` → HTTP 503
  - `PokeApiClientException` → HTTP 502
  - `ConstraintViolationException` / `HandlerMethodValidationException` → HTTP 400

**Exception Hierarchy:**
- Base class or interface not visible in current code; each exception is independent
- All exceptions contain a message passed to `ErrorResponse` DTO
- No exception chaining observed in current code

**Null Handling:**
- Defensive null checks before accessing nested object properties
- Use of ternary operators for null coalescing (e.g., `detail.evolutionChain() != null ? toResponse(detail.evolutionChain()) : null`)
- Stream `.orElse(null)` pattern common for single-value extraction

## Logging

**Framework:** Not configured

**Patterns:**
- No logging framework currently integrated
- JavaDoc comments document important decisions and context instead

## Comments

**When to Comment:**
- Comprehensive JavaDoc on all public classes and methods
- Inline comments explain complex logic (e.g., nested null checks, stream transformations)
- Avoid obvious comments; focus on intent

**JavaDoc/TSDoc Pattern:**
- Class-level JavaDoc explains responsibility and layering constraints
- Method-level JavaDoc includes:
  - Purpose/summary (1-2 sentences)
  - `@param` for each parameter with constraints
  - `@return` for return value
  - `@throws` for checked exceptions (mapped exception docs reference HTTP status mapping)
- Example from `PokemonService`:
  ```java
  /**
   * Fetches the full detail view for a single Pokemon: image, core statistics, a narrative
   * (flavor-text) description, and its evolutionary lineage.
   *
   * @param idOrName the Pokemon's name (lowercase) or its PokeAPI numeric id, as a string
   * @throws PokeApiNotFoundException if no such Pokemon exists
   */
  PokemonDetail getPokemonDetail(String idOrName);
  ```

## Function Design

**Size:**
- Methods kept concise and focused (most 1-20 lines)
- Helper methods extracted for clarity (e.g., `image()`, `englishGenus()` in `PokemonServiceImpl`)
- Stream transformations preferred over explicit loops

**Parameters:**
- Minimal parameter count (typically 1-3)
- Constructor injection for dependencies
- Request parameters use `@RequestParam` with `@Min`, `@Max`, `defaultValue` annotations
- Path variables use `@PathVariable`

**Return Values:**
- Records/immutable value objects returned (no mutable POJOs)
- Early returns when null is possible
- Stream `.toList()` used for collection transformations
- Null returns acceptable for optional fields (e.g., `evolution_chain`, `description`)

## Module Design

**Exports:**
- Public interfaces define contracts (e.g., `PokemonService`)
- Implementations marked `@Service` (Spring component scanning)
- Controllers marked `@RestController`
- All public classes have JavaDoc

**Barrel Files:**
- No barrel/index files observed; imports use fully qualified paths to source files
- Each class lives in its own file

**Layering Pattern:**
- **Controller Layer** (`pokemon.controller`): Maps HTTP requests to service calls, handles DTO conversion
- **Service Layer** (`pokemon.service`): Domain logic, no HTTP or DTO awareness
- **DTO Layer** (`pokemon.dto`): Response objects for outbound HTTP
- **External Client** (`pokeapi.client`): HTTP integration with PokeAPI
- **Common** (`common.exception`): Cross-cutting concerns (exception handling)

**Architectural Constraints:**
- Controllers must NOT directly access external APIs — only through services
- Services must NOT return or accept DTO types — only domain records
- Controllers convert domain records to DTOs via `toResponse()` methods
- All service exceptions propagate (no try/catch in services)

---

*Convention analysis: 2026-08-20*
