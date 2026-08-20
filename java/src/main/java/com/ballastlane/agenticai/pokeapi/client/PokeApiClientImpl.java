package com.ballastlane.agenticai.pokeapi.client;

import com.ballastlane.agenticai.pokeapi.dto.EvolutionChainDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonListResponseDto;
import com.ballastlane.agenticai.pokeapi.dto.PokemonSpeciesDto;
import com.ballastlane.agenticai.pokeapi.exception.PokeApiClientException;
import com.ballastlane.agenticai.pokeapi.exception.PokeApiNotFoundException;
import com.ballastlane.agenticai.pokeapi.exception.PokeApiUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * {@link PokeApiClient} implementation backed by Spring's synchronous {@link RestClient}.
 * Translates transport-level failures (HTTP error statuses, connectivity issues) into the
 * {@code com.ballastlane.agenticai.pokeapi.exception} hierarchy so callers never have to deal
 * with raw {@link org.springframework.web.client.RestClientException} subtypes.
 */
@Component
public class PokeApiClientImpl implements PokeApiClient {

    private final RestClient restClient;

    public PokeApiClientImpl(RestClient.Builder restClientBuilder,
                              @Value("${pokeapi.base-url:https://pokeapi.co/api/v2}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public PokemonListResponseDto listPokemon(int limit, int offset) {
        return getRelative("/pokemon?limit={limit}&offset={offset}", PokemonListResponseDto.class, limit, offset);
    }

    @Override
    public PokemonDto getPokemon(String nameOrId) {
        return getRelative("/pokemon/{nameOrId}", PokemonDto.class, nameOrId);
    }

    @Override
    public PokemonSpeciesDto getPokemonSpecies(String nameOrId) {
        return getRelative("/pokemon-species/{nameOrId}", PokemonSpeciesDto.class, nameOrId);
    }

    @Override
    public EvolutionChainDto getEvolutionChain(String evolutionChainUrl) {
        return getAbsolute(evolutionChainUrl, EvolutionChainDto.class);
    }

    private <T> T getRelative(String uriTemplate, Class<T> responseType, Object... uriVariables) {
        try {
            return restClient.get()
                    .uri(uriTemplate, uriVariables)
                    .retrieve()
                    .body(responseType);
        } catch (HttpClientErrorException.NotFound e) {
            throw new PokeApiNotFoundException("PokeAPI resource not found: " + uriTemplate, e);
        } catch (HttpServerErrorException e) {
            throw new PokeApiUnavailableException("PokeAPI returned a server error for " + uriTemplate, e);
        } catch (HttpStatusCodeException e) {
            throw new PokeApiClientException(
                    "PokeAPI rejected the request for " + uriTemplate + ": " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            throw new PokeApiUnavailableException("PokeAPI is unreachable: " + uriTemplate, e);
        }
    }

    private <T> T getAbsolute(String absoluteUri, Class<T> responseType) {
        try {
            return restClient.get()
                    .uri(java.net.URI.create(absoluteUri))
                    .retrieve()
                    .body(responseType);
        } catch (HttpClientErrorException.NotFound e) {
            throw new PokeApiNotFoundException("PokeAPI resource not found: " + absoluteUri, e);
        } catch (HttpServerErrorException e) {
            throw new PokeApiUnavailableException("PokeAPI returned a server error for " + absoluteUri, e);
        } catch (HttpStatusCodeException e) {
            throw new PokeApiClientException(
                    "PokeAPI rejected the request for " + absoluteUri + ": " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            throw new PokeApiUnavailableException("PokeAPI is unreachable: " + absoluteUri, e);
        }
    }
}
