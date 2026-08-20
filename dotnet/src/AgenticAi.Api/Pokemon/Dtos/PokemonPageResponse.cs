namespace BallastLane.AgenticAi.Api.Pokemon.Dtos;

/// <summary>
/// View DTO for a paginated page of Pokemon list entries.
/// </summary>
public sealed record PokemonPageResponse(
    int Page,
    int Size,
    long TotalCount,
    IReadOnlyList<PokemonSummaryResponse> Items);
