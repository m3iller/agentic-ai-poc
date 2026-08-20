package com.ballastlane.agenticai.pokemon.service;

import java.util.List;

/**
 * One node of a Pokemon's evolutionary lineage, mirroring PokeAPI's evolution-chain "chain_link"
 * shape: the species at this stage, plus the stage(s) it evolves into. Recursive by nature —
 * the detail view (US02) exposes the full chain (every pre-evolution reachable from the root and
 * every subsequent evolution), not just the immediate next stage, since a user browsing detail
 * for a mid-chain Pokemon (e.g. Ivysaur) would otherwise have no way to discover its
 * pre-evolution. See pokemon-detail-view's "Open questions" / this feature's STATUS.md notes for
 * the reasoning.
 */
public record EvolutionStage(String name, List<EvolutionStage> evolvesTo) {
}
