package com.ballastlane.agenticai.pokeapi.client;

import com.ballastlane.agenticai.pokeapi.dto.EvolutionChainDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonListResponseDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonSpeciesDto;
import com.ballastlane.agenticai.pokeapi.exception.PokeApiClientException;
import com.ballastlane.agenticai.pokeapi.exception.PokeApiNotFoundException;
import com.ballastlane.agenticai.pokeapi.exception.PokeApiUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(PokeApiClientImpl.class)
class PokeApiClientImplTest {

    private static final String BASE_URL = "https://pokeapi.co/api/v2";

    @org.springframework.beans.factory.annotation.Autowired
    private PokeApiClientImpl pokeApiClient;

    @org.springframework.beans.factory.annotation.Autowired
    private MockRestServiceServer server;

    @Test
    void listPokemonReturnsDeserializedPage() {
        server.expect(requestTo(BASE_URL + "/pokemon?limit=20&offset=0"))
                .andRespond(withSuccess("""
                        {
                          "count": 1302,
                          "next": "https://pokeapi.co/api/v2/pokemon?offset=20&limit=20",
                          "previous": null,
                          "results": [
                            {"name": "bulbasaur", "url": "https://pokeapi.co/api/v2/pokemon/1/"},
                            {"name": "ivysaur", "url": "https://pokeapi.co/api/v2/pokemon/2/"}
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        PokemonListResponseDto result = pokeApiClient.listPokemon(20, 0);

        assertThat(result.count()).isEqualTo(1302);
        assertThat(result.results()).hasSize(2);
        assertThat(result.results().get(0).name()).isEqualTo("bulbasaur");
        assertThat(result.results().get(0).url()).isEqualTo("https://pokeapi.co/api/v2/pokemon/1/");
    }

    @Test
    void getPokemonReturnsDeserializedDetail() {
        server.expect(requestTo(BASE_URL + "/pokemon/bulbasaur"))
                .andRespond(withSuccess("""
                        {
                          "id": 1,
                          "name": "bulbasaur",
                          "height": 7,
                          "weight": 69,
                          "base_experience": 64,
                          "sprites": {
                            "front_default": "https://example.com/sprite.png",
                            "other": {
                              "official-artwork": { "front_default": "https://example.com/artwork.png" }
                            }
                          },
                          "abilities": [
                            {"ability": {"name": "overgrow", "url": "https://pokeapi.co/api/v2/ability/65/"}, "is_hidden": false, "slot": 1}
                          ],
                          "stats": [
                            {"base_stat": 45, "effort": 0, "stat": {"name": "hp", "url": "https://pokeapi.co/api/v2/stat/1/"}}
                          ],
                          "types": [
                            {"slot": 1, "type": {"name": "grass", "url": "https://pokeapi.co/api/v2/type/12/"}}
                          ],
                          "species": {"name": "bulbasaur", "url": "https://pokeapi.co/api/v2/pokemon-species/1/"}
                        }
                        """, MediaType.APPLICATION_JSON));

        PokemonDto result = pokeApiClient.getPokemon("bulbasaur");

        assertThat(result.id()).isEqualTo(1);
        assertThat(result.name()).isEqualTo("bulbasaur");
        assertThat(result.weight()).isEqualTo(69);
        assertThat(result.sprites().frontDefault()).isEqualTo("https://example.com/sprite.png");
        assertThat(result.sprites().other().officialArtwork().frontDefault())
                .isEqualTo("https://example.com/artwork.png");
        assertThat(result.abilities()).hasSize(1);
        assertThat(result.abilities().get(0).ability().name()).isEqualTo("overgrow");
        assertThat(result.stats().get(0).baseStat()).isEqualTo(45);
        assertThat(result.types().get(0).type().name()).isEqualTo("grass");
        assertThat(result.species().name()).isEqualTo("bulbasaur");
    }

    @Test
    void getPokemonThrowsNotFoundOn404() {
        server.expect(requestTo(BASE_URL + "/pokemon/does-not-exist"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> pokeApiClient.getPokemon("does-not-exist"))
                .isInstanceOf(PokeApiNotFoundException.class);
    }

    @Test
    void getPokemonThrowsUnavailableOn5xx() {
        server.expect(requestTo(BASE_URL + "/pokemon/bulbasaur"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> pokeApiClient.getPokemon("bulbasaur"))
                .isInstanceOf(PokeApiUnavailableException.class);
    }

    @Test
    void getPokemonThrowsClientExceptionOnOther4xx() {
        server.expect(requestTo(BASE_URL + "/pokemon/bulbasaur"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> pokeApiClient.getPokemon("bulbasaur"))
                .isInstanceOf(PokeApiClientException.class);
    }

    @Test
    void getPokemonSpeciesReturnsDeserializedDetail() {
        server.expect(requestTo(BASE_URL + "/pokemon-species/bulbasaur"))
                .andRespond(withSuccess("""
                        {
                          "id": 1,
                          "name": "bulbasaur",
                          "genera": [
                            {"genus": "Seed Pok\\u00e9mon", "language": {"name": "en", "url": "https://pokeapi.co/api/v2/language/9/"}}
                          ],
                          "flavor_text_entries": [
                            {"flavor_text": "A strange seed was planted on its back at birth.", "language": {"name": "en", "url": "https://pokeapi.co/api/v2/language/9/"}, "version": {"name": "red", "url": "https://pokeapi.co/api/v2/version/1/"}}
                          ],
                          "evolution_chain": {"url": "https://pokeapi.co/api/v2/evolution-chain/1/"}
                        }
                        """, MediaType.APPLICATION_JSON));

        PokemonSpeciesDto result = pokeApiClient.getPokemonSpecies("bulbasaur");

        assertThat(result.genera().get(0).genus()).isEqualTo("Seed Pokémon");
        assertThat(result.flavorTextEntries().get(0).flavorText())
                .isEqualTo("A strange seed was planted on its back at birth.");
        assertThat(result.evolutionChain().url())
                .isEqualTo("https://pokeapi.co/api/v2/evolution-chain/1/");
    }

    @Test
    void getEvolutionChainReturnsDeserializedChain() {
        String url = "https://pokeapi.co/api/v2/evolution-chain/1/";
        server.expect(requestTo(url))
                .andRespond(withSuccess("""
                        {
                          "id": 1,
                          "chain": {
                            "species": {"name": "bulbasaur", "url": "https://pokeapi.co/api/v2/pokemon-species/1/"},
                            "evolves_to": [
                              {
                                "species": {"name": "ivysaur", "url": "https://pokeapi.co/api/v2/pokemon-species/2/"},
                                "evolves_to": []
                              }
                            ]
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        EvolutionChainDto result = pokeApiClient.getEvolutionChain(url);

        assertThat(result.id()).isEqualTo(1);
        assertThat(result.chain().species().name()).isEqualTo("bulbasaur");
        assertThat(result.chain().evolvesTo()).hasSize(1);
        assertThat(result.chain().evolvesTo().get(0).species().name()).isEqualTo("ivysaur");
    }
}
