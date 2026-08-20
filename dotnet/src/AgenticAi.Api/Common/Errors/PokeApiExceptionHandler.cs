using BallastLane.AgenticAi.Api.PokeApi.Exceptions;
using Microsoft.AspNetCore.Diagnostics;
using Microsoft.AspNetCore.Http;

namespace BallastLane.AgenticAi.Api.Common.Errors;

/// <summary>
/// Cross-cutting mapping from pokeapi-integration exceptions to HTTP status codes, so no
/// controller needs its own ad-hoc try/catch. 404 for not-found, 503 when PokeAPI itself is
/// unavailable, and 502 for other upstream client errors — the .NET analog of the Java side's
/// <c>@RestControllerAdvice</c>. Invalid <c>page</c>/<c>size</c> query parameters never reach
/// here: they're rejected with 400 by <see cref="Microsoft.AspNetCore.Mvc.ApiControllerAttribute"/>'s
/// automatic model-state validation before the controller action runs.
/// </summary>
public sealed class PokeApiExceptionHandler : IExceptionHandler
{
    public async ValueTask<bool> TryHandleAsync(
        HttpContext httpContext,
        Exception exception,
        CancellationToken cancellationToken)
    {
        var statusCode = exception switch
        {
            PokeApiNotFoundException => StatusCodes.Status404NotFound,
            PokeApiUnavailableException => StatusCodes.Status503ServiceUnavailable,
            PokeApiClientException => StatusCodes.Status502BadGateway,
            _ => (int?)null,
        };

        if (statusCode is null)
        {
            return false;
        }

        httpContext.Response.StatusCode = statusCode.Value;
        await httpContext.Response.WriteAsJsonAsync(new ErrorResponse(exception.Message), cancellationToken);
        return true;
    }
}
