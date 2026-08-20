package com.ballastlane.agenticai.pokemon.service;

import com.ballastlane.agenticai.pokeapi.client.PokeApiClient;
import com.ballastlane.agenticai.pokeapi.dto.EvolutionChainDto;
import com.ballastlane.agenticai.pokeapi.dto.EvolutionChainLinkDto;
import com.ballastlane.agenticai.pokeapi.dto.FlavorTextEntryDto;
import com.ballastlane.agenticai.pokeapi.dto.GenusDto;
import com.ballastlane.agenticai.pokeapi.dto.NamedApiResourceDto;
import com.ballastlane.agenticai.pokeapi.dto.OfficialArtworkDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonAbilityDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonListResponseDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonSpeciesDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonSpritesDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonSpritesOtherDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonStatDto;
import com.ballastlane.agenticai.pokeapi.exception.PokeApiNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
                        new PokemonSpritesOtherDto(new OfficialArtworkDto("https://example.com/artwork.png"))),
                List.of(new PokemonAbilityDto(new NamedApiResourceDto("overgrow", "url"), false, 1),
                        new PokemonAbilityDto(new NamedApiResourceDto("chlorophyll", "url"), true, 2)),
                List.of(new PokemonStatDto(45, 0, new NamedApiResourceDto("hp", "url")),
                        new PokemonStatDto(49, 0, new NamedApiResourceDto("attack", "url"))),
                List.of(),
                new NamedApiResourceDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon-species/1/"));
    }

    private PokemonSpeciesDto bulbasaurSpeciesDto() {
        return new PokemonSpeciesDto(
                1L,
                "bulbasaur",
                List.of(new GenusDto("Seed Pokémon", new NamedApiResourceDto("en", "url")),
                        new GenusDto("Graine Pokémon", new NamedApiResourceDto("fr", "url"))),
                List.of(new FlavorTextEntryDto("A strange seed was planted on its back at birth.\fThe plant sprouts and grows with this Pokémon.",
                                new NamedApiResourceDto("en", "url"), new NamedApiResourceDto("red", "url")),
                        new FlavorTextEntryDto("Une graine étrange fut plantée sur son dos à sa naissance.",
                                new NamedApiResourceDto("fr", "url"), new NamedApiResourceDto("red", "url"))),
                new NamedApiResourceDto(null, "https://pokeapi.co/api/v2/evolution-chain/1/"));
    }

    private EvolutionChainDto bulbasaurEvolutionChainDto() {
        return new EvolutionChainDto(1L,
                new EvolutionChainLinkDto(
                        new NamedApiResourceDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon-species/1/"),
                        List.of(new EvolutionChainLinkDto(
                                new NamedApiResourceDto("ivysaur", "https://pokeapi.co/api/v2/pokemon-species/2/"),
                                List.of(new EvolutionChainLinkDto(
                                        new NamedApiResourceDto("venusaur", "https://pokeapi.co/api/v2/pokemon-species/3/"),
                                        List.of()))))));
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

    @Test
    void getPokemonDetailMapsImageCategoryMassAndSkills() {
        when(pokeApiClient.getPokemon("bulbasaur")).thenReturn(bulbasaurDto());
        when(pokeApiClient.getPokemonSpecies("bulbasaur")).thenReturn(bulbasaurSpeciesDto());
        when(pokeApiClient.getEvolutionChain("https://pokeapi.co/api/v2/evolution-chain/1/"))
                .thenReturn(bulbasaurEvolutionChainDto());

        PokemonDetail result = pokemonService.getPokemonDetail("bulbasaur");

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("bulbasaur");
        assertThat(result.image()).isEqualTo("https://example.com/artwork.png");
        assertThat(result.category()).isEqualTo("Seed Pokémon");
        assertThat(result.mass()).isEqualTo(69);
        assertThat(result.skills()).containsExactly("overgrow", "chlorophyll");
    }

    @Test
    void getPokemonDetailFallsBackToFrontDefaultSpriteWhenNoOfficialArtwork() {
        PokemonDto pokemonWithoutArtwork = new PokemonDto(
                1L, "bulbasaur", 7, 69, 64,
                new PokemonSpritesDto("https://example.com/sprite.png", new PokemonSpritesOtherDto(null)),
                List.of(), List.of(), List.of(),
                new NamedApiResourceDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon-species/1/"));
        when(pokeApiClient.getPokemon("bulbasaur")).thenReturn(pokemonWithoutArtwork);
        when(pokeApiClient.getPokemonSpecies("bulbasaur")).thenReturn(bulbasaurSpeciesDto());
        when(pokeApiClient.getEvolutionChain("https://pokeapi.co/api/v2/evolution-chain/1/"))
                .thenReturn(bulbasaurEvolutionChainDto());

        PokemonDetail result = pokemonService.getPokemonDetail("bulbasaur");

        assertThat(result.image()).isEqualTo("https://example.com/sprite.png");
    }

    @Test
    void getPokemonDetailMapsCoreStatistics() {
        when(pokeApiClient.getPokemon("bulbasaur")).thenReturn(bulbasaurDto());
        when(pokeApiClient.getPokemonSpecies("bulbasaur")).thenReturn(bulbasaurSpeciesDto());
        when(pokeApiClient.getEvolutionChain("https://pokeapi.co/api/v2/evolution-chain/1/"))
                .thenReturn(bulbasaurEvolutionChainDto());

        PokemonDetail result = pokemonService.getPokemonDetail("bulbasaur");

        assertThat(result.stats()).containsExactly(
                new PokemonStat("hp", 45),
                new PokemonStat("attack", 49));
    }

    @Test
    void getPokemonDetailUsesFirstEnglishFlavorTextNormalizingControlCharacters() {
        when(pokeApiClient.getPokemon("bulbasaur")).thenReturn(bulbasaurDto());
        when(pokeApiClient.getPokemonSpecies("bulbasaur")).thenReturn(bulbasaurSpeciesDto());
        when(pokeApiClient.getEvolutionChain("https://pokeapi.co/api/v2/evolution-chain/1/"))
                .thenReturn(bulbasaurEvolutionChainDto());

        PokemonDetail result = pokemonService.getPokemonDetail("bulbasaur");

        assertThat(result.description())
                .isEqualTo("A strange seed was planted on its back at birth. The plant sprouts and grows with this Pokémon.");
    }

    @Test
    void getPokemonDetailMapsFullEvolutionChain() {
        when(pokeApiClient.getPokemon("bulbasaur")).thenReturn(bulbasaurDto());
        when(pokeApiClient.getPokemonSpecies("bulbasaur")).thenReturn(bulbasaurSpeciesDto());
        when(pokeApiClient.getEvolutionChain("https://pokeapi.co/api/v2/evolution-chain/1/"))
                .thenReturn(bulbasaurEvolutionChainDto());

        PokemonDetail result = pokemonService.getPokemonDetail("bulbasaur");

        EvolutionStage root = result.evolutionChain();
        assertThat(root.name()).isEqualTo("bulbasaur");
        assertThat(root.evolvesTo()).hasSize(1);
        EvolutionStage ivysaur = root.evolvesTo().get(0);
        assertThat(ivysaur.name()).isEqualTo("ivysaur");
        assertThat(ivysaur.evolvesTo()).hasSize(1);
        EvolutionStage venusaur = ivysaur.evolvesTo().get(0);
        assertThat(venusaur.name()).isEqualTo("venusaur");
        assertThat(venusaur.evolvesTo()).isEmpty();
    }

    @Test
    void getPokemonDetailPropagatesNotFoundFromClient() {
        when(pokeApiClient.getPokemon("does-not-exist"))
                .thenThrow(new PokeApiNotFoundException("PokeAPI resource not found"));

        assertThatThrownBy(() -> pokemonService.getPokemonDetail("does-not-exist"))
                .isInstanceOf(PokeApiNotFoundException.class);
    }
}
