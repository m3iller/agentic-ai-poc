namespace BallastLane.AgenticAi.Api.PokeApi.Dtos;

/// <summary>
/// Mirrors the response of PokeAPI's GET /pokemon/{id or name} endpoint (fields relevant to
/// this application only — PokeAPI's payload has many more attributes).
/// </summary>
public sealed record PokemonDto(
    long Id,
    string Name,
    int Height,
    int Weight,
    int BaseExperience,
    PokemonSpritesDto Sprites,
    List<PokemonAbilityDto> Abilities,
    List<PokemonStatDto> Stats,
    List<PokemonTypeDto> Types,
    NamedApiResourceDto Species);
