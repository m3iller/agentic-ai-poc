package com.ballastlane.agenticai.pokeapi.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

/**
 * Mirrors the response of PokeAPI's GET /pokemon-species/{id or name} endpoint (fields
 * relevant to this application only).
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PokemonSpeciesDto(
        long id,
        String name,
        List<GenusDto> genera,
        List<FlavorTextEntryDto> flavorTextEntries,
        NamedApiResourceDto evolutionChain) {
}
