package com.ballastlane.agenticai.pokeapi.exception;

/**
 * Base type for all errors raised by the PokeAPI integration layer. Callers (services in other
 * features) should catch this — or one of its subtypes — rather than letting transport-level
 * exceptions (e.g. from the underlying HTTP client) leak past this layer.
 */
public class PokeApiException extends RuntimeException {

    public PokeApiException(String message) {
        super(message);
    }

    public PokeApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
