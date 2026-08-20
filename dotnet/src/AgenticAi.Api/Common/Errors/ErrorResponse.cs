namespace BallastLane.AgenticAi.Api.Common.Errors;

/// <summary>
/// Minimal well-formed error body returned by <see cref="PokeApiExceptionHandler"/> so failures
/// are never surfaced as an unformatted stack trace/500.
/// </summary>
public sealed record ErrorResponse(string Message);
