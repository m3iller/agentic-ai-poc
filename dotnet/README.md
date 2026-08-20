# .NET migration (target stack)

Reserved for the .NET 10 / ASP.NET Core port of the Java implementation under [`../java`](../java).
Nothing has been scaffolded here yet — the `dotnet` CLI isn't available in the environment this
placeholder was created in, so no project files were hand-written and left unverified. The
`dotnet-agent` (see `.claude/agents/dotnet-agent.md`) scaffolds the solution as its first task,
verifying with `dotnet build`/`dotnet test` as it goes.

## Intended structure (mirrors `java/`'s layered architecture)

```
dotnet/
├── AgenticAi.sln
├── src/
│   └── AgenticAi.Api/            # Controllers, Services, Repositories per feature — see dotnet-agent.md
└── tests/
    └── AgenticAi.Api.Tests/      # xUnit
```

- **Target framework**: `net10.0`
- **Test framework**: xUnit
- **Persistence**: EF Core (analog to Spring Data JPA on the Java side)
- **Specs**: shared with the Java implementation at [`../specs`](../specs) — not duplicated per
  stack. `specs/STATUS.md` tracks Java and .NET progress in separate columns.
