package com.ballastlane.agenticai.pokemon.dto;

import java.util.List;

/**
 * View DTO for one node of the evolutionary lineage shown on the detail view (see
 * pokemon-detail-view/US02). Recursive, mirroring {@code pokemon.service.EvolutionStage}.
 */
public record EvolutionStageResponse(String name, List<EvolutionStageResponse> evolvesTo) {
}
