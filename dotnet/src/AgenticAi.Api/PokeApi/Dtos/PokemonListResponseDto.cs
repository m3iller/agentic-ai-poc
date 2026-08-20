namespace BallastLane.AgenticAi.Api.PokeApi.Dtos;

/// <summary>
/// Mirrors the response of PokeAPI's GET /pokemon list endpoint.
/// </summary>
public sealed record PokemonListResponseDto(
    int Count,
    string? Next,
    string? Previous,
    List<NamedApiResourceDto> Results);
