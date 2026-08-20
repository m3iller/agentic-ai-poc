package com.ballastlane.agenticai.pokeapi.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record FlavorTextEntryDto(
        String flavorText,
        NamedApiResourceDto language,
        NamedApiResourceDto version) {
}
