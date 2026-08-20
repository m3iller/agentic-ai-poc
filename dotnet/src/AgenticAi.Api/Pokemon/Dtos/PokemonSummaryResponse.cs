namespace BallastLane.AgenticAi.Api.Pokemon.Dtos;

/// <summary>
/// View DTO for a single Pokemon list entry (see pokemon-enumeration/US01): sprite, category,
/// mass, and skills (abilities), plus the identifiers needed to later request its detail view.
/// </summary>
public sealed record PokemonSummaryResponse(
    long Id,
    string Name,
    string? Sprite,
    string? Category,
    int Mass,
    IReadOnlyList<string> Skills);
