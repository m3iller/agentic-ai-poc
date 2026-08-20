package com.ballastlane.agenticai.pokemon.controller;

import com.ballastlane.agenticai.pokemon.dto.PokemonPageResponse;
import com.ballastlane.agenticai.pokemon.dto.PokemonSummaryResponse;
import com.ballastlane.agenticai.pokemon.service.PokemonPage;
import com.ballastlane.agenticai.pokemon.service.PokemonService;
import com.ballastlane.agenticai.pokemon.service.PokemonSummary;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * HTTP-facing entry point for browsing Pokemon (pokemon-enumeration/US01). Maps query
 * parameters to a {@link PokemonService} call and the resulting {@link PokemonPage} (Model) to
 * a {@link PokemonPageResponse} (View) — no business logic lives here.
 */
@RestController
@RequestMapping("/api/pokemon")
@Validated
public class PokemonController {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final PokemonService pokemonService;

    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    @GetMapping
    public PokemonPageResponse list(
            @RequestParam(name = "page", defaultValue = "" + DEFAULT_PAGE) @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "" + DEFAULT_SIZE) @Min(1) @Max(MAX_SIZE) int size) {
        PokemonPage result = pokemonService.listPokemon(page, size);
        return toResponse(result);
    }

    private PokemonPageResponse toResponse(PokemonPage page) {
        List<PokemonSummaryResponse> items = page.items().stream()
                .map(this::toResponse)
                .toList();
        return new PokemonPageResponse(page.page(), page.size(), page.totalCount(), items);
    }

    private PokemonSummaryResponse toResponse(PokemonSummary summary) {
        return new PokemonSummaryResponse(
                summary.id(),
                summary.name(),
                summary.sprite(),
                summary.category(),
                summary.mass(),
                summary.skills());
    }
}
