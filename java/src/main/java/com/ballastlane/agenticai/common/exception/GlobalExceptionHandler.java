package com.ballastlane.agenticai.common.exception;

import com.ballastlane.agenticai.pokeapi.exception.PokeApiClientException;
import com.ballastlane.agenticai.pokeapi.exception.PokeApiNotFoundException;
import com.ballastlane.agenticai.pokeapi.exception.PokeApiUnavailableException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * Cross-cutting mapping from domain/service exceptions to HTTP status codes, so no controller
 * needs its own ad-hoc try/catch. 404 for not-found, 400 for malformed/invalid request
 * parameters, and well-formed error bodies for upstream (PokeAPI) failures instead of an
 * unformatted 500 — see pokeapi-integration's "well-formed error response" acceptance
 * criterion, which applies once a controller sits in front of the client.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PokeApiNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(PokeApiNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(PokeApiUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleUnavailable(PokeApiUnavailableException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(PokeApiClientException.class)
    public ResponseEntity<ErrorResponse> handleClientException(PokeApiClientException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class})
    public ResponseEntity<ErrorResponse> handleInvalidRequestParameters(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("Invalid request parameters"));
    }
}
