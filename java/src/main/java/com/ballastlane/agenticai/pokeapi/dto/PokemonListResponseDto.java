package com.ballastlane.agenticai.pokeapi.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

/**
 * Mirrors the response of PokeAPI's GET /pokemon list endpoint.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PokemonListResponseDto(
        int count,
        String next,
        String previous,
        List<NamedApiResourceDto> results) {
}
