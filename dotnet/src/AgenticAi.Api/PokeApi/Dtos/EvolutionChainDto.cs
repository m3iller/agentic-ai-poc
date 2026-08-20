namespace BallastLane.AgenticAi.Api.PokeApi.Dtos;

/// <summary>
/// Mirrors the response of PokeAPI's GET /evolution-chain/{id} endpoint.
/// </summary>
public sealed record EvolutionChainDto(long Id, EvolutionChainLinkDto Chain);
