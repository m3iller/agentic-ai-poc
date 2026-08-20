package com.ballastlane.agenticai.pokeapi.exception;

/**
 * Raised when PokeAPI responds 404 for a requested resource (e.g. an unknown Pokemon
 * name/id). Callers translate this into their own not-found semantics (e.g. HTTP 404).
 */
public class PokeApiNotFoundException extends PokeApiException {

    public PokeApiNotFoundException(String message) {
        super(message);
    }

    public PokeApiNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
