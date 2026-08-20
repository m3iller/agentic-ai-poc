namespace BallastLane.AgenticAi.Api.Tests.TestSupport;

/// <summary>
/// Minimal <see cref="HttpMessageHandler"/> test double: hands every outgoing request to a
/// caller-supplied responder function, capturing the last request seen so tests can assert on
/// it. Avoids pulling in a mocking library just to fake <see cref="HttpClient"/> transport.
/// </summary>
public sealed class StubHttpMessageHandler : HttpMessageHandler
{
    private readonly Func<HttpRequestMessage, HttpResponseMessage> _responder;

    public StubHttpMessageHandler(Func<HttpRequestMessage, HttpResponseMessage> responder)
    {
        _responder = responder;
    }

    public HttpRequestMessage? LastRequest { get; private set; }

    protected override Task<HttpResponseMessage> SendAsync(
        HttpRequestMessage request, CancellationToken cancellationToken)
    {
        LastRequest = request;
        return Task.FromResult(_responder(request));
    }

    /// <summary>
    /// Builds an <see cref="HttpClient"/> wired to this handler, with the given base address
    /// (must end with a trailing slash so relative request URIs resolve under it rather than
    /// replacing its path).
    /// </summary>
    public HttpClient ToHttpClient(string baseAddress)
    {
        return new HttpClient(this) { BaseAddress = new Uri(baseAddress) };
    }
}
