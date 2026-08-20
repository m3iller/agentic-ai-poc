namespace BallastLane.AgenticAi.Api.PokeApi.Exceptions;

/// <summary>
/// Raised when PokeAPI responds with a 4xx status other than 404 (e.g. a malformed request from
/// this application). Distinct from <see cref="PokeApiNotFoundException"/> so callers can
/// special-case "not found" without conflating it with other client-side errors.
/// </summary>
public sealed class PokeApiClientException : PokeApiException
{
    public PokeApiClientException(string message) : base(message)
    {
    }

    public PokeApiClientException(string message, Exception cause) : base(message, cause)
    {
    }
}
