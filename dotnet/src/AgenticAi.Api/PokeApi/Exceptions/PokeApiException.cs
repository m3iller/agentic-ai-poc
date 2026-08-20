namespace BallastLane.AgenticAi.Api.PokeApi.Exceptions;

/// <summary>
/// Base type for all errors raised by the PokeAPI integration layer. Callers (services in other
/// features) should catch this — or one of its subtypes — rather than letting transport-level
/// exceptions (e.g. from the underlying <see cref="System.Net.Http.HttpClient"/>) leak past this
/// layer.
/// </summary>
public class PokeApiException : Exception
{
    public PokeApiException(string message) : base(message)
    {
    }

    public PokeApiException(string message, Exception cause) : base(message, cause)
    {
    }
}
