package com.ballastlane.agenticai.pokemon.service;

/**
 * Owns the business rules for browsing Pokemon (User Story 01). Speaks only domain types
 * ({@link PokemonPage}/{@link PokemonSummary}) — never HTTP or DTO types, per this project's
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
}
