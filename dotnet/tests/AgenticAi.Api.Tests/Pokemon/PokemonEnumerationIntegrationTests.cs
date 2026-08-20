using System.Net;
using System.Net.Http.Json;
using BallastLane.AgenticAi.Api.Common.Errors;
using BallastLane.AgenticAi.Api.PokeApi.Exceptions;
using BallastLane.AgenticAi.Api.Pokemon.Dtos;
using BallastLane.AgenticAi.Api.Pokemon.Services;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.DependencyInjection.Extensions;
using Moq;

namespace BallastLane.AgenticAi.Api.Tests.Pokemon;

/// <summary>
/// Thin end-to-end slice covering the pieces that only manifest at the HTTP pipeline level:
/// automatic 400s for invalid <c>page</c>/<c>size</c> query parameters, and the exception-handler
/// middleware mapping pokeapi-integration exceptions to 404/503/502. Business logic itself is
/// covered by <see cref="Controllers.PokemonControllerTests"/> and
/// <see cref="Services.PokemonServiceTests"/>, so <see cref="IPokemonService"/> is stubbed here
/// rather than hitting the real PokeAPI.
/// </summary>
public class PokemonEnumerationIntegrationTests : IClassFixture<WebApplicationFactory<Program>>
{
    private readonly WebApplicationFactory<Program> _factory;

    public PokemonEnumerationIntegrationTests(WebApplicationFactory<Program> factory)
    {
        _factory = factory;
    }

    private WebApplicationFactory<Program> WithService(Mock<IPokemonService> pokemonService)
    {
        return _factory.WithWebHostBuilder(builder =>
        {
            builder.ConfigureServices(services =>
            {
                services.RemoveAll<IPokemonService>();
                services.AddScoped<IPokemonService>(_ => pokemonService.Object);
            });
        });
    }

    [Fact]
    public async Task Get_ReturnsOk_WithDefaultPageAndSize_WhenNoParametersSupplied()
    {
        var pokemonService = new Mock<IPokemonService>();
        pokemonService
            .Setup(s => s.ListPokemonAsync(1, 20, It.IsAny<CancellationToken>()))
            .ReturnsAsync(new PokemonPage(1, 20, 0, []));
        var client = WithService(pokemonService).CreateClient();

        var response = await client.GetAsync("/api/pokemon");

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        var body = await response.Content.ReadFromJsonAsync<PokemonPageResponse>();
        Assert.Equal(1, body!.Page);
        Assert.Equal(20, body.Size);
    }

    [Theory]
    [InlineData("?page=0")]
    [InlineData("?page=-1")]
    [InlineData("?size=0")]
    [InlineData("?size=101")]
    public async Task Get_ReturnsBadRequest_ForInvalidPageOrSize(string query)
    {
        var client = WithService(new Mock<IPokemonService>()).CreateClient();

        var response = await client.GetAsync($"/api/pokemon{query}");

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
    }

    [Fact]
    public async Task Get_ReturnsNotFound_WhenServiceThrowsPokeApiNotFound()
    {
        var pokemonService = new Mock<IPokemonService>();
        pokemonService
            .Setup(s => s.ListPokemonAsync(1, 20, It.IsAny<CancellationToken>()))
            .ThrowsAsync(new PokeApiNotFoundException("not found"));
        var client = WithService(pokemonService).CreateClient();

        var response = await client.GetAsync("/api/pokemon");

        Assert.Equal(HttpStatusCode.NotFound, response.StatusCode);
        var body = await response.Content.ReadFromJsonAsync<ErrorResponse>();
        Assert.Equal("not found", body!.Message);
    }

    [Fact]
    public async Task Get_ReturnsServiceUnavailable_WhenServiceThrowsPokeApiUnavailable()
    {
        var pokemonService = new Mock<IPokemonService>();
        pokemonService
            .Setup(s => s.ListPokemonAsync(1, 20, It.IsAny<CancellationToken>()))
            .ThrowsAsync(new PokeApiUnavailableException("unavailable"));
        var client = WithService(pokemonService).CreateClient();

        var response = await client.GetAsync("/api/pokemon");

        Assert.Equal(HttpStatusCode.ServiceUnavailable, response.StatusCode);
    }

    [Fact]
    public async Task Get_ReturnsBadGateway_WhenServiceThrowsPokeApiClientException()
    {
        var pokemonService = new Mock<IPokemonService>();
        pokemonService
            .Setup(s => s.ListPokemonAsync(1, 20, It.IsAny<CancellationToken>()))
            .ThrowsAsync(new PokeApiClientException("bad gateway"));
        var client = WithService(pokemonService).CreateClient();

        var response = await client.GetAsync("/api/pokemon");

        Assert.Equal(HttpStatusCode.BadGateway, response.StatusCode);
    }
}
