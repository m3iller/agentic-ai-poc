namespace BallastLane.AgenticAi.Api.PokeApi.Dtos;

public sealed record PokemonStatDto(
    int BaseStat,
    int Effort,
    NamedApiResourceDto Stat);
