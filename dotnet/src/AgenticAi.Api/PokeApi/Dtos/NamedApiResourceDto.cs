namespace BallastLane.AgenticAi.Api.PokeApi.Dtos;

/// <summary>
/// Mirrors PokeAPI's ubiquitous "NamedAPIResource" shape: a name plus a URL pointing at the
/// full resource. Used for references such as species, abilities, stats, and types.
/// </summary>
public sealed record NamedApiResourceDto(string Name, string Url);
