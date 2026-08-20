package com.ballastlane.agenticai.pokemon.dto;

import java.util.List;

/**
 * View DTO for a single Pokemon list entry (see pokemon-enumeration/US01): sprite, category,
 * mass, and skills (abilities), plus the identifiers needed to later request its detail view.
 */
public record PokemonSummaryResponse(
        long id,
        String name,
        String sprite,
        String category,
        int mass,
        List<String> skills) {
}
