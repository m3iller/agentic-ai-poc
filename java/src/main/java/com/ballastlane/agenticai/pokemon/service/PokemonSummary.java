package com.ballastlane.agenticai.pokemon.service;

import java.util.List;

/**
 * Domain object built by {@link PokemonService} for a single Pokemon list entry, combining
 * PokeAPI's pokemon + pokemon-species resources. There is no local persistence for this feature
 * yet (see pokemon-data-synchronization for that), so this is a plain in-memory value, not an
 * {@code @Entity} — the Controller maps it to a {@code pokemon.dto} response before it leaves
 * the application.
 */
public record PokemonSummary(
        long id,
        String name,
        String sprite,
        String category,
        int mass,
        List<String> skills) {
}
