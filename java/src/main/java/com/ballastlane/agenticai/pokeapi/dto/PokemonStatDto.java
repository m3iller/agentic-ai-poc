package com.ballastlane.agenticai.pokeapi.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PokemonStatDto(
        int baseStat,
        int effort,
        NamedApiResourceDto stat) {
}
