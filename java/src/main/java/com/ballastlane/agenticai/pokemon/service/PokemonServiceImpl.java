package com.ballastlane.agenticai.pokemon.service;

import com.ballastlane.agenticai.pokeapi.client.PokeApiClient;
import com.ballastlane.agenticai.pokeapi.dto.EvolutionChainLinkDto;
import com.ballastlane.agenticai.pokeapi.dto.FlavorTextEntryDto;
import com.ballastlane.agenticai.pokeapi.dto.GenusDto;
import com.ballastlane.agenticai.pokeapi.dto.NamedApiResourceDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonListResponseDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonSpeciesDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonStatDto;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {@link PokemonService} implementation that calls {@link PokeApiClient} directly per request
 * and combines the pokemon-list, pokemon, pokemon-species, and evolution-chain PokeAPI resources
 * into {@link PokemonSummary}/{@link PokemonDetail} entries. There is no local
 * persistence/caching yet (see pokemon-data-synchronization for that) — every call fans out to
 * PokeAPI.
 */
@Service
public class PokemonServiceImpl implements PokemonService {

    private static final String ENGLISH_LANGUAGE = "en";

    private final PokeApiClient pokeApiClient;

    public PokemonServiceImpl(PokeApiClient pokeApiClient) {
        this.pokeApiClient = pokeApiClient;
    }

    @Override
    public PokemonDetail getPokemonDetail(String idOrName) {
        PokemonDto pokemon = pokeApiClient.getPokemon(idOrName);
        PokemonSpeciesDto species = pokeApiClient.getPokemonSpecies(idOrName);

        List<String> skills = pokemon.abilities().stream()
                .map(ability -> ability.ability().name())
                .toList();

        List<PokemonStat> stats = pokemon.stats().stream()
                .map(this::toStat)
                .toList();

        EvolutionStage evolutionChain = species.evolutionChain() != null
                ? toEvolutionStage(pokeApiClient.getEvolutionChain(species.evolutionChain().url()).chain())
                : null;

        return new PokemonDetail(
                pokemon.id(),
                pokemon.name(),
                image(pokemon),
                englishGenus(species),
                pokemon.weight(),
                skills,
                stats,
                englishDescription(species),
                evolutionChain);
    }

    private String image(PokemonDto pokemon) {
        if (pokemon.sprites() == null) {
            return null;
        }
        if (pokemon.sprites().other() != null && pokemon.sprites().other().officialArtwork() != null
                && pokemon.sprites().other().officialArtwork().frontDefault() != null) {
            return pokemon.sprites().other().officialArtwork().frontDefault();
        }
        return pokemon.sprites().frontDefault();
    }

    private PokemonStat toStat(PokemonStatDto statDto) {
        return new PokemonStat(statDto.stat().name(), statDto.baseStat());
    }

    private String englishDescription(PokemonSpeciesDto species) {
        if (species.flavorTextEntries() == null) {
            return null;
        }
        return species.flavorTextEntries().stream()
                .filter(entry -> ENGLISH_LANGUAGE.equals(entry.language().name()))
                .map(FlavorTextEntryDto::flavorText)
                .map(this::normalizeFlavorText)
                .findFirst()
                .orElse(null);
    }

    private String normalizeFlavorText(String flavorText) {
        return flavorText.replace('\f', ' ').replace('\n', ' ');
    }

    private EvolutionStage toEvolutionStage(EvolutionChainLinkDto link) {
        List<EvolutionStage> evolvesTo = link.evolvesTo().stream()
                .map(this::toEvolutionStage)
                .toList();
        return new EvolutionStage(link.species().name(), evolvesTo);
    }

    @Override
    public PokemonPage listPokemon(int page, int size) {
        int offset = (page - 1) * size;
        PokemonListResponseDto listResponse = pokeApiClient.listPokemon(size, offset);

        List<PokemonSummary> items = listResponse.results().stream()
                .map(this::toSummary)
                .toList();

        return new PokemonPage(page, size, listResponse.count(), items);
    }

    private PokemonSummary toSummary(NamedApiResourceDto reference) {
        PokemonDto pokemon = pokeApiClient.getPokemon(reference.name());
        PokemonSpeciesDto species = pokeApiClient.getPokemonSpecies(reference.name());

        List<String> skills = pokemon.abilities().stream()
                .map(ability -> ability.ability().name())
                .toList();

        return new PokemonSummary(
                pokemon.id(),
                pokemon.name(),
                pokemon.sprites() != null ? pokemon.sprites().frontDefault() : null,
                englishGenus(species),
                pokemon.weight(),
                skills);
    }

    private String englishGenus(PokemonSpeciesDto species) {
        if (species.genera() == null) {
            return null;
        }
        return species.genera().stream()
                .filter(genus -> ENGLISH_LANGUAGE.equals(genus.language().name()))
                .map(GenusDto::genus)
                .findFirst()
                .orElse(null);
    }
}
