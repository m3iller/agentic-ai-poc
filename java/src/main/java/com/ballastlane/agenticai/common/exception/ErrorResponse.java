package com.ballastlane.agenticai.common.exception;

/**
 * Minimal well-formed error body returned by {@link GlobalExceptionHandler} so failures are
 * never surfaced as an unformatted stack trace/500.
 */
public record ErrorResponse(String message) {
}
