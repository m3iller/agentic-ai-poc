package com.ballastlane.agenticai.pokemon.service;

import java.util.List;

/**
 * Domain object built by {@link PokemonService} for the detail view (US02), combining PokeAPI's
 * pokemon + pokemon-species + evolution-chain resources. Like {@link PokemonSummary}, there is
 * no local persistence for this feature yet — this is a plain in-memory value, not an
 * {@code @Entity} — the Controller maps it to a {@code pokemon.dto} response before it leaves
 * the application.
 */
public record PokemonDetail(
        long id,
        String name,
        String image,
        String category,
        int mass,
        List<String> skills,
        List<PokemonStat> stats,
        String description,
        EvolutionStage evolutionChain) {
}
