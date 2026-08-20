using System.Text.Json.Serialization;

namespace BallastLane.AgenticAi.Api.PokeApi.Dtos;

/// <summary>
/// PokeAPI's "sprites.other" object contains several alternate artwork sets keyed by
/// kebab-case names (e.g. "official-artwork") that don't follow snake_case, hence the explicit
/// <see cref="JsonPropertyNameAttribute"/>.
/// </summary>
public sealed record PokemonSpritesOtherDto(
    [property: JsonPropertyName("official-artwork")] OfficialArtworkDto? OfficialArtwork);
