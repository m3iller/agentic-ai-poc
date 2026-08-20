package com.ballastlane.agenticai.pokemon.controller;

import com.ballastlane.agenticai.pokeapi.exception.PokeApiUnavailableException;
import com.ballastlane.agenticai.pokemon.service.PokemonPage;
import com.ballastlane.agenticai.pokemon.service.PokemonService;
import com.ballastlane.agenticai.pokemon.service.PokemonSummary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PokemonController.class)
class PokemonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PokemonService pokemonService;

    private PokemonPage samplePage(int page, int size) {
        PokemonSummary summary = new PokemonSummary(
                1L, "bulbasaur", "https://example.com/sprite.png", "Seed Pokémon", 69,
                List.of("overgrow", "chlorophyll"));
        return new PokemonPage(page, size, 1302, List.of(summary));
    }

    @Test
    void listWithNoParamsUsesDefaultPage() throws Exception {
        when(pokemonService.listPokemon(1, 20)).thenReturn(samplePage(1, 20));

        mockMvc.perform(get("/api/pokemon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalCount").value(1302))
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.items[0].name").value("bulbasaur"))
                .andExpect(jsonPath("$.items[0].sprite").value("https://example.com/sprite.png"))
                .andExpect(jsonPath("$.items[0].category").value("Seed Pokémon"))
                .andExpect(jsonPath("$.items[0].mass").value(69))
                .andExpect(jsonPath("$.items[0].skills[0]").value("overgrow"))
                .andExpect(jsonPath("$.items[0].skills[1]").value("chlorophyll"));

        verify(pokemonService).listPokemon(1, 20);
    }

    @Test
    void listWithPageAndSizeParamsPassesThemToService() throws Exception {
        when(pokemonService.listPokemon(anyInt(), anyInt())).thenReturn(samplePage(2, 10));

        mockMvc.perform(get("/api/pokemon").param("page", "2").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(10));

        verify(pokemonService).listPokemon(2, 10);
    }

    @Test
    void listWithPageLessThanOneReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/pokemon").param("page", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listWithSizeAboveMaxReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/pokemon").param("size", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listWithSizeLessThanOneReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/pokemon").param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listWhenPokeApiUnavailableReturnsServiceUnavailable() throws Exception {
        when(pokemonService.listPokemon(anyInt(), anyInt()))
                .thenThrow(new PokeApiUnavailableException("PokeAPI is unreachable"));

        mockMvc.perform(get("/api/pokemon"))
                .andExpect(status().isServiceUnavailable());
    }
}
