package com.ballastlane.agenticai.pokemon.service;

import com.ballastlane.agenticai.pokeapi.client.PokeApiClient;
import com.ballastlane.agenticai.pokeapi.dto.GenusDto;
import com.ballastlane.agenticai.pokeapi.dto.NamedApiResourceDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonListResponseDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonSpeciesDto;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {@link PokemonService} implementation that calls {@link PokeApiClient} directly per request
 * and combines the pokemon-list, pokemon, and pokemon-species PokeAPI resources into
 * {@link PokemonSummary} entries. There is no local persistence/caching yet (see
 * pokemon-data-synchronization for that) — every call fans out to PokeAPI.
 */
@Service
public class PokemonServiceImpl implements PokemonService {

    private static final String ENGLISH_LANGUAGE = "en";

    private final PokeApiClient pokeApiClient;

    public PokemonServiceImpl(PokeApiClient pokeApiClient) {
        this.pokeApiClient = pokeApiClient;
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
