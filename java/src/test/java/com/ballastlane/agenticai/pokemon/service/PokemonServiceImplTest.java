package com.ballastlane.agenticai.pokemon.service;

import com.ballastlane.agenticai.pokeapi.client.PokeApiClient;
import com.ballastlane.agenticai.pokeapi.dto.GenusDto;
import com.ballastlane.agenticai.pokeapi.dto.NamedApiResourceDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonAbilityDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonListResponseDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonSpeciesDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonSpritesDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonSpritesOtherDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PokemonServiceImplTest {

    @Mock
    private PokeApiClient pokeApiClient;

    private PokemonServiceImpl pokemonService;

    @BeforeEach
    void setUp() {
        pokemonService = new PokemonServiceImpl(pokeApiClient);
    }

    private PokemonDto bulbasaurDto() {
        return new PokemonDto(
                1L,
                "bulbasaur",
                7,
                69,
                64,
                new PokemonSpritesDto("https://example.com/sprite.png",
                        new PokemonSpritesOtherDto(null)),
                List.of(new PokemonAbilityDto(new NamedApiResourceDto("overgrow", "url"), false, 1),
                        new PokemonAbilityDto(new NamedApiResourceDto("chlorophyll", "url"), true, 2)),
                List.of(),
                List.of(),
                new NamedApiResourceDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon-species/1/"));
    }

    private PokemonSpeciesDto bulbasaurSpeciesDto() {
        return new PokemonSpeciesDto(
                1L,
                "bulbasaur",
                List.of(new GenusDto("Seed Pokémon", new NamedApiResourceDto("en", "url")),
                        new GenusDto("Graine Pokémon", new NamedApiResourceDto("fr", "url"))),
                List.of(),
                new NamedApiResourceDto(null, "https://pokeapi.co/api/v2/evolution-chain/1/"));
    }

    @Test
    void listPokemonUsesDefaultPageAndSizeToComputeOffset() {
        when(pokeApiClient.listPokemon(eq(20), eq(0)))
                .thenReturn(new PokemonListResponseDto(1302, null, null,
                        List.of(new NamedApiResourceDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"))));
        when(pokeApiClient.getPokemon("bulbasaur")).thenReturn(bulbasaurDto());
        when(pokeApiClient.getPokemonSpecies("bulbasaur")).thenReturn(bulbasaurSpeciesDto());

        PokemonPage result = pokemonService.listPokemon(1, 20);

        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(20);
        assertThat(result.totalCount()).isEqualTo(1302);
        assertThat(result.items()).hasSize(1);
    }

    @Test
    void listPokemonComputesOffsetFromPageAndSize() {
        when(pokeApiClient.listPokemon(eq(10), eq(20)))
                .thenReturn(new PokemonListResponseDto(1302, null, null, List.of()));

        pokemonService.listPokemon(3, 10);

        org.mockito.Mockito.verify(pokeApiClient).listPokemon(10, 20);
    }

    @Test
    void listPokemonMapsSpriteCategoryMassAndSkills() {
        when(pokeApiClient.listPokemon(eq(20), eq(0)))
                .thenReturn(new PokemonListResponseDto(1302, null, null,
                        List.of(new NamedApiResourceDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"))));
        when(pokeApiClient.getPokemon("bulbasaur")).thenReturn(bulbasaurDto());
        when(pokeApiClient.getPokemonSpecies("bulbasaur")).thenReturn(bulbasaurSpeciesDto());

        PokemonPage result = pokemonService.listPokemon(1, 20);

        PokemonSummary summary = result.items().get(0);
        assertThat(summary.id()).isEqualTo(1L);
        assertThat(summary.name()).isEqualTo("bulbasaur");
        assertThat(summary.sprite()).isEqualTo("https://example.com/sprite.png");
        assertThat(summary.category()).isEqualTo("Seed Pokémon");
        assertThat(summary.mass()).isEqualTo(69);
        assertThat(summary.skills()).containsExactly("overgrow", "chlorophyll");
    }
}
