package com.ballastlane.agenticai.pokeapi.exception;

/**
 * Raised when PokeAPI responds with a 4xx status other than 404 (e.g. a malformed request from
 * this application). Distinct from {@link PokeApiNotFoundException} so callers can special-case
 * "not found" without conflating it with other client-side errors.
 */
public class PokeApiClientException extends PokeApiException {

    public PokeApiClientException(String message) {
        super(message);
    }

    public PokeApiClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
