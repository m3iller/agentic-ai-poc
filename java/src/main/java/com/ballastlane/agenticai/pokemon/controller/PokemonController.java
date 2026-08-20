package com.ballastlane.agenticai.pokemon.controller;

import com.ballastlane.agenticai.pokemon.dto.EvolutionStageResponse;
import com.ballastlane.agenticai.pokemon.dto.PokemonDetailResponse;
import com.ballastlane.agenticai.pokemon.dto.PokemonPageResponse;
import com.ballastlane.agenticai.pokemon.dto.PokemonStatResponse;
import com.ballastlane.agenticai.pokemon.dto.PokemonSummaryResponse;
import com.ballastlane.agenticai.pokemon.service.EvolutionStage;
import com.ballastlane.agenticai.pokemon.service.PokemonDetail;
import com.ballastlane.agenticai.pokemon.service.PokemonPage;
import com.ballastlane.agenticai.pokemon.service.PokemonService;
import com.ballastlane.agenticai.pokemon.service.PokemonStat;
import com.ballastlane.agenticai.pokemon.service.PokemonSummary;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * HTTP-facing entry point for browsing Pokemon (pokemon-enumeration/US01) and viewing a single
 * Pokemon's full detail (pokemon-detail-view/US02). Maps request parameters to a
 * {@link PokemonService} call and the resulting Model ({@link PokemonPage}/{@link PokemonDetail})
 * to a View ({@link PokemonPageResponse}/{@link PokemonDetailResponse}) — no business logic
 * lives here. A missing Pokemon (US02's 404 case) surfaces as a
 * {@code PokeApiNotFoundException} from the service, translated to HTTP 404 by
 * {@code common.exception.GlobalExceptionHandler}, not handled here.
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

    @GetMapping("/{idOrName}")
    public PokemonDetailResponse getDetail(@PathVariable("idOrName") String idOrName) {
        PokemonDetail detail = pokemonService.getPokemonDetail(idOrName);
        return toResponse(detail);
    }

    private PokemonDetailResponse toResponse(PokemonDetail detail) {
        List<PokemonStatResponse> stats = detail.stats().stream()
                .map(this::toResponse)
                .toList();
        return new PokemonDetailResponse(
                detail.id(),
                detail.name(),
                detail.image(),
                detail.category(),
                detail.mass(),
                detail.skills(),
                stats,
                detail.description(),
                detail.evolutionChain() != null ? toResponse(detail.evolutionChain()) : null);
    }

    private PokemonStatResponse toResponse(PokemonStat stat) {
        return new PokemonStatResponse(stat.name(), stat.value());
    }

    private EvolutionStageResponse toResponse(EvolutionStage stage) {
        List<EvolutionStageResponse> evolvesTo = stage.evolvesTo().stream()
                .map(this::toResponse)
                .toList();
        return new EvolutionStageResponse(stage.name(), evolvesTo);
    }
}
