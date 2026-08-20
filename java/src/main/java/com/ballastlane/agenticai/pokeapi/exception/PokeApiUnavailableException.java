package com.ballastlane.agenticai.pokeapi.exception;

/**
 * Raised when PokeAPI cannot be reached at all (network/timeout failure) or responds with a
 * server error (5xx), i.e. the upstream dependency itself is the problem, not the request.
 */
public class PokeApiUnavailableException extends PokeApiException {

    public PokeApiUnavailableException(String message) {
        super(message);
    }

    public PokeApiUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
