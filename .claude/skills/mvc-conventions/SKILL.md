---
name: mvc-conventions
description: Model-View-Controller naming/folder conventions layered on top of this project's existing Controller→Service→Repository architecture, shared by java-agent and dotnet-agent. Use when implementing or reviewing a feature in either java/ or dotnet/ to keep Model/View/Controller responsibilities consistent across both stacks. Trigger phrases: "use MVC for this feature", "where does this DTO/entity belong", "is this a Model or a View concern".
---

This project decided **against** adopting full Clean Architecture (separate Domain/Application/
Infrastructure/Presentation rings) — `java-agent` and `dotnet-agent` both keep the simpler
Controller → Service → Repository layering already established. This skill does **not** change
that decision. It maps standard MVC terminology onto the layers that already exist, so "Model",
"View", and "Controller" mean one consistent thing across both `java/` and `dotnet/`, instead of
each stack (or each feature) inventing its own DTO/entity naming ad hoc.

## The mapping

This is a REST API with no server-rendered templates — the frontend is a separate client
(`specs/features/frontend-client.md`) consuming JSON. So:

- **Model** — the domain/persistence entity: Java's `@Entity` class, .NET's EF Core entity class.
  Lives where it already lives today — inside the **Repository** layer (`<feature>/repository` /
  `<Feature>/Repositories`) — this skill doesn't relocate it. It's called out as "the Model" so
  everyone uses that word for it, not "the entity" in one PR and "the domain object" in another.
- **View** — the request/response DTO: what the Controller accepts and returns. This is the
  JSON *shape*, not a rendered template — there is no `.jsp`/`.cshtml`/Razor view anywhere in this
  project. Give DTOs their own subfolder so they read as a distinct layer rather than being
  inlined into the Controller file:
  - Java: `<feature>/dto` (records) — e.g. `pokemon/dto/PokemonResponse.java`
  - .NET: `<Feature>/Dtos` (records) — e.g. `Pokemon/Dtos/PokemonResponse.cs`
- **Controller** — unchanged from the existing architecture: `<feature>/controller` /
  `<Feature>/Controllers`. HTTP concerns only, maps View (DTOs) to/from Service calls. Spring
  `@RestController` and ASP.NET Core `[ApiController]` are both already MVC-pattern controllers —
  nothing new required here, just keep them free of business logic as already specified.

**Service and Repository are not part of MVC and don't change.** Service still owns business
rules and depends on the Repository interface; Repository still owns persistence. MVC describes
the Controller/Model/View triangle — it doesn't have an opinion on how business logic or
persistence are organized beneath that, so this project's existing Service/Repository split
stays exactly as `java-agent.md`/`dotnet-agent.md` already document it.

## Rules

- **Controller never returns a Model (entity) directly** — always map Model → View (DTO) before
  returning, even when the shapes look identical today. This is what keeps persistence schema
  changes from silently becoming API breaking changes later.
- **Service speaks Model, not View** — DTOs don't cross into the Service layer; if a Service
  method's signature has a DTO type in it, that mapping belongs in the Controller instead.
- **The View/DTO layer has zero business logic** — no validation beyond shape (`@Valid`/
  DataAnnotations are fine, they're just payload-shape checks), no calls to Service or Repository
  from inside a DTO class.
- Naming: suffix response DTOs `...Response`, request DTOs `...Request` in both stacks, so a
  reviewer can tell Controller-facing View types from Model entities at a glance without opening
  the file.

## Out of scope

- Reopening the Clean Architecture decision — if a feature genuinely seems to need Domain/
  Application/Infrastructure separation, say so and let the user decide; don't restructure
  unilaterally.
- Server-rendered views (Razor/Thymeleaf/JSP) — not applicable, this is a JSON API.
- Anything about the frontend-client feature's own MVC/MVVM/component structure — that's a
  separate concern (`specs/features/frontend-client.md`), not this backend convention.
