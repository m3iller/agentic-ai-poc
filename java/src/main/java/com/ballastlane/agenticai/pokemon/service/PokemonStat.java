package com.ballastlane.agenticai.pokemon.service;

/**
 * A single core statistic (e.g. "hp", "attack") shown on the detail view (US02). Mirrors
 * PokeAPI's pokemon "stats" entries: {@code name} is the stat's PokeAPI name, {@code value} is
 * its base_stat value.
 */
public record PokemonStat(String name, int value) {
}
