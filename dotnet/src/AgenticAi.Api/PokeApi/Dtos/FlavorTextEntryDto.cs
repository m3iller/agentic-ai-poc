namespace BallastLane.AgenticAi.Api.PokeApi.Dtos;

public sealed record FlavorTextEntryDto(
    string FlavorText,
    NamedApiResourceDto Language,
    NamedApiResourceDto Version);
