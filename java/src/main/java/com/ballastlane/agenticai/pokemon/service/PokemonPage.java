package com.ballastlane.agenticai.pokemon.service;

import java.util.List;

/**
 * Domain object representing one page of {@link PokemonSummary} results, as returned by
 * {@link PokemonService#listPokemon(int, int)}.
 */
public record PokemonPage(
        int page,
        int size,
        long totalCount,
        List<PokemonSummary> items) {
}
