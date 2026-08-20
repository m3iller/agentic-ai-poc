namespace BallastLane.AgenticAi.Api.Pokemon.Services;

/// <summary>
/// Domain object built by <see cref="IPokemonService"/> for a single Pokemon list entry,
/// combining PokeAPI's pokemon + pokemon-species resources. There is no local persistence for
/// this feature yet (see pokemon-data-synchronization for that), so this is a plain in-memory
/// value, not an EF Core entity — the Controller maps it to a <c>Pokemon/Dtos</c> response before
/// it leaves the application.
/// </summary>
public sealed record PokemonSummary(
    long Id,
    string Name,
    string? Sprite,
    string? Category,
    int Mass,
    IReadOnlyList<string> Skills);
