using BallastLane.AgenticAi.Api.PokeApi.Dtos;
using BallastLane.AgenticAi.Api.PokeApi.Exceptions;

namespace BallastLane.AgenticAi.Api.PokeApi.Clients;

/// <summary>
/// The seam through which the rest of the application talks to the external PokeAPI
/// (https://pokeapi.co/docs/v2). Other features' services depend on this interface — never on
/// <see cref="PokeApiClient"/> directly — so they stay testable and the transport can be swapped.
///
/// <para>All methods surface upstream failures as one of the
/// <see cref="BallastLane.AgenticAi.Api.PokeApi.Exceptions"/> types instead of letting
/// transport-level exceptions escape.</para>
/// </summary>
public interface IPokeApiClient
{
    /// <summary>
    /// Fetches a page of the Pokemon list (name + resource URL only; no per-entry detail).
    /// </summary>
    /// <param name="limit">max number of entries to return</param>
    /// <param name="offset">number of entries to skip</param>
    /// <exception cref="PokeApiUnavailableException">if PokeAPI is unreachable or returns a server error</exception>
    /// <exception cref="PokeApiClientException">if the request itself is rejected (e.g. invalid params)</exception>
    Task<PokemonListResponseDto> ListPokemonAsync(int limit, int offset, CancellationToken cancellationToken = default);

    /// <summary>
    /// Fetches full detail for a single Pokemon resource.
    /// </summary>
    /// <param name="nameOrId">the Pokemon's name (lowercase) or its PokeAPI numeric id, as a string</param>
    /// <exception cref="PokeApiNotFoundException">if no such Pokemon exists</exception>
    /// <exception cref="PokeApiUnavailableException">if PokeAPI is unreachable or returns a server error</exception>
    Task<PokemonDto> GetPokemonAsync(string nameOrId, CancellationToken cancellationToken = default);

    /// <summary>
    /// Fetches species-level detail (genus/category, flavor text descriptions, and a reference
    /// to the evolution chain) for a Pokemon.
    /// </summary>
    /// <param name="nameOrId">the Pokemon (species) name or PokeAPI numeric id, as a string</param>
    /// <exception cref="PokeApiNotFoundException">if no such species exists</exception>
    /// <exception cref="PokeApiUnavailableException">if PokeAPI is unreachable or returns a server error</exception>
    Task<PokemonSpeciesDto> GetPokemonSpeciesAsync(string nameOrId, CancellationToken cancellationToken = default);

    /// <summary>
    /// Fetches an evolution chain by its absolute PokeAPI URL (as referenced by
    /// <see cref="PokemonSpeciesDto.EvolutionChain"/>).
    /// </summary>
    /// <exception cref="PokeApiNotFoundException">if the referenced evolution chain doesn't exist</exception>
    /// <exception cref="PokeApiUnavailableException">if PokeAPI is unreachable or returns a server error</exception>
    Task<EvolutionChainDto> GetEvolutionChainAsync(string evolutionChainUrl, CancellationToken cancellationToken = default);
}
