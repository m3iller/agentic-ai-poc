namespace BallastLane.AgenticAi.Api.PokeApi.Dtos;

/// <summary>
/// A single node of an evolution chain: the species at this stage plus the stages it evolves
/// into. Recursive by nature (mirrors PokeAPI's "chain_link" object).
/// </summary>
public sealed record EvolutionChainLinkDto(
    NamedApiResourceDto Species,
    List<EvolutionChainLinkDto> EvolvesTo);
