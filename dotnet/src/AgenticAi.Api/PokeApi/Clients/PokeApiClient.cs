using System.Net;
using System.Net.Http.Json;
using System.Text.Json;
using BallastLane.AgenticAi.Api.PokeApi.Dtos;
using BallastLane.AgenticAi.Api.PokeApi.Exceptions;

namespace BallastLane.AgenticAi.Api.PokeApi.Clients;

/// <summary>
/// <see cref="IPokeApiClient"/> implementation backed by a plain <see cref="HttpClient"/>.
/// Translates transport-level failures (HTTP error statuses, connectivity issues) into the
/// <c>BallastLane.AgenticAi.Api.PokeApi.Exceptions</c> hierarchy so callers never have to deal
/// with raw <see cref="HttpRequestException"/>s or non-success status codes.
/// </summary>
/// <remarks>
/// The injected <see cref="HttpClient"/> is expected to have its <see cref="HttpClient.BaseAddress"/>
/// already set to the PokeAPI base URL (with a trailing slash), e.g. via
/// <c>IHttpClientFactory</c>'s typed-client registration. Request paths below are relative
/// (no leading slash) so they resolve underneath that base address rather than replacing it.
/// </remarks>
public sealed class PokeApiClient : IPokeApiClient
{
    private static readonly JsonSerializerOptions SerializerOptions = new()
    {
        PropertyNamingPolicy = JsonNamingPolicy.SnakeCaseLower,
    };

    private readonly HttpClient _httpClient;

    public PokeApiClient(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public Task<PokemonListResponseDto> ListPokemonAsync(int limit, int offset, CancellationToken cancellationToken = default)
        => GetAsync<PokemonListResponseDto>($"pokemon?limit={limit}&offset={offset}", cancellationToken);

    public Task<PokemonDto> GetPokemonAsync(string nameOrId, CancellationToken cancellationToken = default)
        => GetAsync<PokemonDto>($"pokemon/{Uri.EscapeDataString(nameOrId)}", cancellationToken);

    public Task<PokemonSpeciesDto> GetPokemonSpeciesAsync(string nameOrId, CancellationToken cancellationToken = default)
        => GetAsync<PokemonSpeciesDto>($"pokemon-species/{Uri.EscapeDataString(nameOrId)}", cancellationToken);

    public Task<EvolutionChainDto> GetEvolutionChainAsync(string evolutionChainUrl, CancellationToken cancellationToken = default)
        => GetAsync<EvolutionChainDto>(evolutionChainUrl, cancellationToken);

    private async Task<T> GetAsync<T>(string requestUri, CancellationToken cancellationToken)
    {
        HttpResponseMessage response;
        try
        {
            response = await _httpClient.GetAsync(requestUri, cancellationToken);
        }
        catch (HttpRequestException ex)
        {
            throw new PokeApiUnavailableException($"PokeAPI is unreachable: {requestUri}", ex);
        }
        catch (TaskCanceledException ex) when (!cancellationToken.IsCancellationRequested)
        {
            // A timeout surfaces as a TaskCanceledException that wasn't requested by our own token.
            throw new PokeApiUnavailableException($"PokeAPI request timed out: {requestUri}", ex);
        }

        using (response)
        {
            if (response.IsSuccessStatusCode)
            {
                var result = await response.Content.ReadFromJsonAsync<T>(SerializerOptions, cancellationToken);
                return result ?? throw new PokeApiException($"PokeAPI returned an empty body for {requestUri}");
            }

            throw response.StatusCode switch
            {
                HttpStatusCode.NotFound => new PokeApiNotFoundException($"PokeAPI resource not found: {requestUri}"),
                _ when (int)response.StatusCode >= 500 => new PokeApiUnavailableException(
                    $"PokeAPI returned a server error for {requestUri}: {(int)response.StatusCode}"),
                _ => new PokeApiClientException(
                    $"PokeAPI rejected the request for {requestUri}: {(int)response.StatusCode}"),
            };
        }
    }
}
