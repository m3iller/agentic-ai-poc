package com.ballastlane.agenticai.pokemon.dto;

/**
 * View DTO for a single core statistic entry on the detail view (see pokemon-detail-view/US02).
 */
public record PokemonStatResponse(String name, int value) {
}
