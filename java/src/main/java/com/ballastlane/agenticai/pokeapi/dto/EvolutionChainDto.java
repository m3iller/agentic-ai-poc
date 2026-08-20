package com.ballastlane.agenticai.pokeapi.dto;

/**
 * Mirrors the response of PokeAPI's GET /evolution-chain/{id} endpoint.
 */
public record EvolutionChainDto(long id, EvolutionChainLinkDto chain) {
}
