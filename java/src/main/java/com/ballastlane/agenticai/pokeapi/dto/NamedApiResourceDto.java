package com.ballastlane.agenticai.pokeapi.dto;

/**
 * Mirrors PokeAPI's ubiquitous "NamedAPIResource" shape: a name plus a URL pointing at the
 * full resource. Used for references such as species, abilities, stats, and types.
 */
public record NamedApiResourceDto(String name, String url) {
}
