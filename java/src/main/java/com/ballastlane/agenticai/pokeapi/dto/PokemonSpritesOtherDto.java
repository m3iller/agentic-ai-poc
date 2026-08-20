package com.ballastlane.agenticai.pokeapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * PokeAPI's "sprites.other" object contains several alternate artwork sets keyed by
 * kebab-case names (e.g. "official-artwork") that don't follow snake_case, hence the explicit
 * {@link JsonProperty}.
 */
public record PokemonSpritesOtherDto(
        @JsonProperty("official-artwork") OfficialArtworkDto officialArtwork) {
}
