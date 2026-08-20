namespace BallastLane.AgenticAi.Api.PokeApi.Dtos;

public sealed record PokemonAbilityDto(
    NamedApiResourceDto Ability,
    bool IsHidden,
    int Slot);
