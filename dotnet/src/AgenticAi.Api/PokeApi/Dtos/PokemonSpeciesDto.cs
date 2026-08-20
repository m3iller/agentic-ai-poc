namespace BallastLane.AgenticAi.Api.PokeApi.Dtos;

/// <summary>
/// Mirrors the response of PokeAPI's GET /pokemon-species/{id or name} endpoint (fields
/// relevant to this application only).
/// </summary>
public sealed record PokemonSpeciesDto(
    long Id,
    string Name,
    List<GenusDto> Genera,
    List<FlavorTextEntryDto> FlavorTextEntries,
    NamedApiResourceDto EvolutionChain);
