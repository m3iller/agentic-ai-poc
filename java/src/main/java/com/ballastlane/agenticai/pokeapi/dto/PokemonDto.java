package com.ballastlane.agenticai.pokeapi.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

/**
 * Mirrors the response of PokeAPI's GET /pokemon/{id or name} endpoint (fields relevant to
 * this application only — PokeAPI's payload has many more attributes).
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PokemonDto(
        long id,
        String name,
        int height,
        int weight,
        int baseExperience,
        PokemonSpritesDto sprites,
        List<PokemonAbilityDto> abilities,
        List<PokemonStatDto> stats,
        List<PokemonTypeDto> types,
        NamedApiResourceDto species) {
}
