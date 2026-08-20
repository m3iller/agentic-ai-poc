namespace BallastLane.AgenticAi.Api.PokeApi.Exceptions;

/// <summary>
/// Raised when PokeAPI responds 404 for a requested resource (e.g. an unknown Pokemon
/// name/id). Callers translate this into their own not-found semantics (e.g. HTTP 404).
/// </summary>
public sealed class PokeApiNotFoundException : PokeApiException
{
    public PokeApiNotFoundException(string message) : base(message)
    {
    }

    public PokeApiNotFoundException(string message, Exception cause) : base(message, cause)
    {
    }
}
