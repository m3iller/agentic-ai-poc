package com.ballastlane.agenticai.pokemon.dto;

import java.util.List;

/**
 * View DTO for a paginated page of Pokemon list entries.
 */
public record PokemonPageResponse(
        int page,
        int size,
        long totalCount,
        List<PokemonSummaryResponse> items) {
}
