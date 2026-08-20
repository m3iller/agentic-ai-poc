namespace BallastLane.AgenticAi.Api.PokeApi.Exceptions;

/// <summary>
/// Raised when PokeAPI cannot be reached at all (network/timeout failure) or responds with a
/// server error (5xx), i.e. the upstream dependency itself is the problem, not the request.
/// </summary>
public sealed class PokeApiUnavailableException : PokeApiException
{
    public PokeApiUnavailableException(string message) : base(message)
    {
    }

    public PokeApiUnavailableException(string message, Exception cause) : base(message, cause)
    {
    }
}
