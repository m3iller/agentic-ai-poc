# Technology Stack

**Analysis Date:** 2026-08-20

## Languages

**Primary:**
- Java 21 - Backend REST API implementation in `java/` directory
- C# (.NET 10) - Backend REST API migration target in `dotnet/` directory

**Secondary:**
- Node.js 22.22.1 - Meta-tooling and self-improvement scripts in `tools/self-improve/`

## Runtime

**Environment:**
- JVM (Java 21) - runs Java Spring Boot application
- .NET Runtime 10.0 - runs ASP.NET Core application
- Node.js 22.22.1 - runs self-improvement and tooling scripts

**Package Manager:**
- Maven 3.9.11 (Java dependencies) - uses `java/pom.xml`
- NuGet (implicit in .NET via csproj files) - uses `dotnet/src/AgenticAi.Api/AgenticAi.Api.csproj` and `dotnet/tests/AgenticAi.Api.Tests/AgenticAi.Api.Tests.csproj`
- npm 10.9.4 (Node.js tooling) - uses root `package.json`
- Lockfile: Maven uses `pom.xml` directly; NuGet locks are in `.csproj` files; npm has no lockfile (minimal dependencies)

## Frameworks

**Core - Java:**
- Spring Boot 3.5.6 - RESTful API framework
- Spring Web (`spring-boot-starter-web`) - HTTP server, REST controllers, MVC
- Spring Validation (`spring-boot-starter-validation`) - Bean validation (JSR-303/JSR-380)
- Jackson (implicit in Spring Boot) - JSON serialization/deserialization

**Core - .NET:**
- ASP.NET Core 10.0.11 - RESTful API framework via `Microsoft.NET.Sdk.Web`
- System.Text.Json - JSON serialization/deserialization (implicit)

**Testing - Java:**
- JUnit 5 (Jupiter) - test runner via `junit-jupiter` dependency
- Spring Boot Test (`spring-boot-starter-test`) - Spring test utilities, MockMvc, embedded servlet container

**Testing - .NET:**
- xUnit 2.9.3 - test runner
- Moq 4.20.72 - mocking framework
- Microsoft.AspNetCore.Mvc.Testing 10.0.11 - integration testing, WebApplicationFactory
- coverlet.collector 6.0.4 - code coverage data collection

**Build/Dev - Java:**
- Maven Compiler Plugin 3.9.0 (implicit via maven-compiler-source/target properties) - Java source/target version 21
- Maven Surefire Plugin 3.2.5 - test execution

**Build/Dev - .NET:**
- dotnet CLI (implicit) - build, test, run commands

## Key Dependencies

**Java - Critical:**
- `org.springframework.boot:spring-boot-starter-web:3.5.6` - HTTP request routing, REST annotations (`@RestController`, `@GetMapping`), embedded Tomcat servlet container
- `org.springframework.web.client:RestClient` (part of Spring Framework, managed by Spring Boot) - synchronous HTTP client for external API calls; replaces deprecated RestTemplate
- `com.fasterxml.jackson.core:jackson-databind` (implicit via Spring Boot) - JSON parsing with configurable deserialization (e.g., `fail-on-unknown-properties: false` in `application.yml`)
- `org.springframework.boot:spring-boot-starter-validation:3.5.6` - Jakarta Bean Validation (JSR-380), constraint annotations (`@Min`, `@Max`)

**Java - Testing:**
- `org.junit.jupiter:junit-jupiter` (version managed by Spring Boot) - test annotations, assertions
- `org.springframework.boot:spring-boot-starter-test:3.5.6` - embedded test server, Spring test context

**.NET - Critical:**
- `Microsoft.AspNetCore.OpenApi:10.0.11` - OpenAPI/Swagger support for API documentation
- `System.Net.Http.HttpClient` (built-in) - typed HTTP client for external calls via `IHttpClientFactory`
- `System.Text.Json` (built-in) - JSON serialization with configurable naming policies (e.g., snake_case)

**.NET - Testing:**
- `xunit:2.9.3` - test annotations, assertions
- `Moq:4.20.72` - mock object creation
- `Microsoft.AspNetCore.Mvc.Testing:10.0.11` - `WebApplicationFactory<Program>` for in-memory test server
- `Microsoft.NET.Test.Sdk:17.14.1` - test discovery and execution
- `coverlet.collector:6.0.4` - code coverage instrumentation

**Meta-Tooling - Node.js:**
- Node.js built-ins only; no external npm dependencies declared in `package.json`
- Relies on `claude` CLI tool (Claude Code environment) being available on PATH

## Configuration

**Environment - Java:**
- `java/src/main/resources/application.yml` - Spring Boot configuration
  - Spring application name: `agentic-ai`
  - Jackson deserialization: `fail-on-unknown-properties: false` (allows PokéAPI responses with unknown fields)
  - PokéAPI base URL: `https://pokeapi.co/api/v2` (configurable via `${pokeapi.base-url}` property)
- Configuration is injected via Spring's property resolution (command-line, environment variables, or property files can override defaults)

**Environment - .NET:**
- Default configuration loaded from `appsettings.json` (not visible in current scan, using ASP.NET Core defaults)
- PokéAPI base URL: configurable via `IConfiguration["PokeApi:BaseUrl"]`, defaults to `https://pokeapi.co/api/v2/` in `Program.cs`
- Exception handling configured in `Program.cs`: `AddExceptionHandler<PokeApiExceptionHandler>()` + `AddProblemDetails()`
- OpenAPI/Swagger mapping in Development environment

**Environment - Build:**
- Java: Maven compiler source/target version 21 (set in `pom.xml` `<maven.compiler.source>` and `<maven.compiler.target>`)
- .NET: Target framework `net10.0` (C# 13 implicit with .NET 10), nullable reference types enabled, implicit usings enabled

## Platform Requirements

**Development:**
- Java stack: Java 21 JDK, Maven 3.9.11 or later
- .NET stack: .NET SDK 10.0 or later
- Node.js: Node.js 22.22.1 (for `tools/self-improve` meta-tooling)
- OS: macOS (Darwin), Linux, or Windows with standard build tools

**Production:**
- Java: Java 21 Runtime (JRE) or Docker container with Java 21; deployed as JAR file built by `mvn package` → `java/target/agentic-ai-1.0-SNAPSHOT.jar`
- .NET: .NET 10 Runtime or Docker container with .NET 10; deployed as running the `dotnet run` output or standalone deployment

**External Runtime Dependencies:**
- Both stacks depend on external PokéAPI service (`https://pokeapi.co/api/v2`) being reachable via HTTPS

---

*Stack analysis: 2026-08-20*
