namespace BallastLane.AgenticAi.Api.Pokemon.Services;

/// <summary>
/// Domain object representing one page of <see cref="PokemonSummary"/> results, as returned by
/// <see cref="IPokemonService.ListPokemonAsync"/>.
/// </summary>
public sealed record PokemonPage(
    int Page,
    int Size,
    long TotalCount,
    IReadOnlyList<PokemonSummary> Items);
