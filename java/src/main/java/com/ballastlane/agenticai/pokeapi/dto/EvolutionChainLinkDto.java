package com.ballastlane.agenticai.pokeapi.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

/**
 * A single node of an evolution chain: the species at this stage plus the stages it evolves
 * into. Recursive by nature (mirrors PokeAPI's "chain_link" object).
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record EvolutionChainLinkDto(
        NamedApiResourceDto species,
        List<EvolutionChainLinkDto> evolvesTo) {
}
