package com.ballastlane.agenticai.pokeapi.client;

import com.ballastlane.agenticai.pokeapi.dto.EvolutionChainDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonListResponseDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonSpeciesDto;
import com.ballastlane.agenticai.pokeapi.exception.PokeApiClientException;
import com.ballastlane.agenticai.pokeapi.exception.PokeApiNotFoundException;
import com.ballastlane.agenticai.pokeapi.exception.PokeApiUnavailableException;

/**
 * The seam through which the rest of the application talks to the external PokeAPI
 * (https://pokeapi.co/docs/v2). Other features' services depend on this interface — never on
 * {@link PokeApiClientImpl} directly — so they stay testable and the transport can be swapped.
 *
 * <p>All methods surface upstream failures as one of the {@code com.ballastlane.agenticai.pokeapi.exception}
 * types instead of letting transport-level exceptions escape.</p>
 */
public interface PokeApiClient {

    /**
     * Fetches a page of the Pokemon list (name + resource URL only; no per-entry detail).
     *
     * @param limit  max number of entries to return
     * @param offset number of entries to skip
     * @throws PokeApiUnavailableException if PokeAPI is unreachable or returns a server error
     * @throws PokeApiClientException      if the request itself is rejected (e.g. invalid params)
     */
    PokemonListResponseDto listPokemon(int limit, int offset);

    /**
     * Fetches full detail for a single Pokemon resource.
     *
     * @param nameOrId the Pokemon's name (lowercase) or its PokeAPI numeric id, as a string
     * @throws PokeApiNotFoundException    if no such Pokemon exists
     * @throws PokeApiUnavailableException if PokeAPI is unreachable or returns a server error
     */
    PokemonDto getPokemon(String nameOrId);

    /**
     * Fetches species-level detail (genus/category, flavor text descriptions, and a reference
     * to the evolution chain) for a Pokemon.
     *
     * @param nameOrId the Pokemon (species) name or PokeAPI numeric id, as a string
     * @throws PokeApiNotFoundException    if no such species exists
     * @throws PokeApiUnavailableException if PokeAPI is unreachable or returns a server error
     */
    PokemonSpeciesDto getPokemonSpecies(String nameOrId);

    /**
     * Fetches an evolution chain by its absolute PokeAPI URL (as referenced by
     * {@link PokemonSpeciesDto#evolutionChain()}).
     *
     * @throws PokeApiNotFoundException    if the referenced evolution chain doesn't exist
     * @throws PokeApiUnavailableException if PokeAPI is unreachable or returns a server error
     */
    EvolutionChainDto getEvolutionChain(String evolutionChainUrl);
}
