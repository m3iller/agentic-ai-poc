package com.ballastlane.agenticai.pokemon.dto;

import java.util.List;

/**
 * View DTO for a Pokemon's full detail view (see pokemon-detail-view/US02): image, category,
 * mass, skills, core statistics, a narrative description, and the evolutionary lineage.
 */
public record PokemonDetailResponse(
        long id,
        String name,
        String image,
        String category,
        int mass,
        List<String> skills,
        List<PokemonStatResponse> stats,
        String description,
        EvolutionStageResponse evolutionChain) {
}
