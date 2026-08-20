package com.ballastlane.agenticai.pokemon.service;

import com.ballastlane.agenticai.pokeapi.exception.PokeApiNotFoundException;

/**
 * Owns the business rules for browsing Pokemon (User Story 01) and viewing a single Pokemon's
 * full detail (User Story 02). Speaks only domain types ({@link PokemonPage}/
 * {@link PokemonSummary}/{@link PokemonDetail}) — never HTTP or DTO types, per this project's
 * layering conventions. Depends on the {@code pokeapi-integration} client, never on a
 * Controller or DTO.
 */
public interface PokemonService {

    /**
     * Fetches one page of Pokemon, each including sprite, category, mass, and skills.
     *
     * @param page 1-based page number
     * @param size number of entries per page
     */
    PokemonPage listPokemon(int page, int size);

    /**
     * Fetches the full detail view for a single Pokemon: image, core statistics, a narrative
     * (flavor-text) description, and its evolutionary lineage.
     *
     * @param idOrName the Pokemon's name (lowercase) or its PokeAPI numeric id, as a string —
     *                 same identifier scheme as {@link PokemonSummary#id()}/{@link PokemonSummary#name()}
     * @throws PokeApiNotFoundException if no such Pokemon exists — translated to an HTTP 404 by
     *                                   {@code common.exception.GlobalExceptionHandler}
     */
    PokemonDetail getPokemonDetail(String idOrName);
}
