using System.Net;
using System.Net.Http;
using System.Text;
using BallastLane.AgenticAi.Api.PokeApi.Clients;
using BallastLane.AgenticAi.Api.PokeApi.Exceptions;
using BallastLane.AgenticAi.Api.Tests.TestSupport;

namespace BallastLane.AgenticAi.Api.Tests.PokeApi.Clients;

public class PokeApiClientTests
{
    private const string BaseUrl = "https://pokeapi.co/api/v2/";

    private static PokeApiClient CreateClient(Func<HttpRequestMessage, HttpResponseMessage> responder,
        out StubHttpMessageHandler handler)
    {
        handler = new StubHttpMessageHandler(responder);
        var httpClient = handler.ToHttpClient(BaseUrl);
        return new PokeApiClient(httpClient);
    }

    private static HttpResponseMessage JsonResponse(HttpStatusCode status, string json)
    {
        return new HttpResponseMessage(status)
        {
            Content = new StringContent(json, Encoding.UTF8, "application/json"),
        };
    }

    [Fact]
    public async Task ListPokemonAsync_ReturnsDeserializedPage()
    {
        var client = CreateClient(request =>
        {
            Assert.Equal("https://pokeapi.co/api/v2/pokemon?limit=20&offset=0", request.RequestUri!.ToString());
            return JsonResponse(HttpStatusCode.OK, """
                {
                  "count": 1302,
                  "next": "https://pokeapi.co/api/v2/pokemon?offset=20&limit=20",
                  "previous": null,
                  "results": [
                    {"name": "bulbasaur", "url": "https://pokeapi.co/api/v2/pokemon/1/"},
                    {"name": "ivysaur", "url": "https://pokeapi.co/api/v2/pokemon/2/"}
                  ]
                }
                """);
        }, out _);

        var result = await client.ListPokemonAsync(20, 0);

        Assert.Equal(1302, result.Count);
        Assert.Equal(2, result.Results.Count);
        Assert.Equal("bulbasaur", result.Results[0].Name);
        Assert.Equal("https://pokeapi.co/api/v2/pokemon/1/", result.Results[0].Url);
    }

    [Fact]
    public async Task GetPokemonAsync_ReturnsDeserializedDetail()
    {
        var client = CreateClient(request =>
        {
            Assert.Equal("https://pokeapi.co/api/v2/pokemon/bulbasaur", request.RequestUri!.ToString());
            return JsonResponse(HttpStatusCode.OK, """
                {
                  "id": 1,
                  "name": "bulbasaur",
                  "height": 7,
                  "weight": 69,
                  "base_experience": 64,
                  "sprites": {
                    "front_default": "https://example.com/sprite.png",
                    "other": {
                      "official-artwork": { "front_default": "https://example.com/artwork.png" }
                    }
                  },
                  "abilities": [
                    {"ability": {"name": "overgrow", "url": "https://pokeapi.co/api/v2/ability/65/"}, "is_hidden": false, "slot": 1}
                  ],
                  "stats": [
                    {"base_stat": 45, "effort": 0, "stat": {"name": "hp", "url": "https://pokeapi.co/api/v2/stat/1/"}}
                  ],
                  "types": [
                    {"slot": 1, "type": {"name": "grass", "url": "https://pokeapi.co/api/v2/type/12/"}}
                  ],
                  "species": {"name": "bulbasaur", "url": "https://pokeapi.co/api/v2/pokemon-species/1/"}
                }
                """);
        }, out _);

        var result = await client.GetPokemonAsync("bulbasaur");

        Assert.Equal(1, result.Id);
        Assert.Equal("bulbasaur", result.Name);
        Assert.Equal(69, result.Weight);
        Assert.Equal("https://example.com/sprite.png", result.Sprites.FrontDefault);
        Assert.Equal("https://example.com/artwork.png", result.Sprites.Other!.OfficialArtwork!.FrontDefault);
        Assert.Single(result.Abilities);
        Assert.Equal("overgrow", result.Abilities[0].Ability.Name);
        Assert.Equal(45, result.Stats[0].BaseStat);
        Assert.Equal("grass", result.Types[0].Type.Name);
        Assert.Equal("bulbasaur", result.Species.Name);
    }

    [Fact]
    public async Task GetPokemonAsync_ThrowsNotFound_On404()
    {
        var client = CreateClient(_ => new HttpResponseMessage(HttpStatusCode.NotFound), out _);

        await Assert.ThrowsAsync<PokeApiNotFoundException>(() => client.GetPokemonAsync("does-not-exist"));
    }

    [Fact]
    public async Task GetPokemonAsync_ThrowsUnavailable_On5xx()
    {
        var client = CreateClient(_ => new HttpResponseMessage(HttpStatusCode.InternalServerError), out _);

        await Assert.ThrowsAsync<PokeApiUnavailableException>(() => client.GetPokemonAsync("bulbasaur"));
    }

    [Fact]
    public async Task GetPokemonAsync_ThrowsClientException_OnOther4xx()
    {
        var client = CreateClient(_ => new HttpResponseMessage(HttpStatusCode.BadRequest), out _);

        await Assert.ThrowsAsync<PokeApiClientException>(() => client.GetPokemonAsync("bulbasaur"));
    }

    [Fact]
    public async Task GetPokemonAsync_ThrowsUnavailable_WhenUnreachable()
    {
        var client = CreateClient(
            _ => throw new HttpRequestException("connection refused"), out _);

        await Assert.ThrowsAsync<PokeApiUnavailableException>(() => client.GetPokemonAsync("bulbasaur"));
    }

    [Fact]
    public async Task GetPokemonSpeciesAsync_ReturnsDeserializedDetail()
    {
        var client = CreateClient(request =>
        {
            Assert.Equal("https://pokeapi.co/api/v2/pokemon-species/bulbasaur", request.RequestUri!.ToString());
            return JsonResponse(HttpStatusCode.OK, """
                {
                  "id": 1,
                  "name": "bulbasaur",
                  "genera": [
                    {"genus": "Seed Pokémon", "language": {"name": "en", "url": "https://pokeapi.co/api/v2/language/9/"}}
                  ],
                  "flavor_text_entries": [
                    {"flavor_text": "A strange seed was planted on its back at birth.", "language": {"name": "en", "url": "https://pokeapi.co/api/v2/language/9/"}, "version": {"name": "red", "url": "https://pokeapi.co/api/v2/version/1/"}}
                  ],
                  "evolution_chain": {"name": null, "url": "https://pokeapi.co/api/v2/evolution-chain/1/"}
                }
                """);
        }, out _);

        var result = await client.GetPokemonSpeciesAsync("bulbasaur");

        Assert.Equal("Seed Pokémon", result.Genera[0].Genus);
        Assert.Equal("A strange seed was planted on its back at birth.", result.FlavorTextEntries[0].FlavorText);
        Assert.Equal("https://pokeapi.co/api/v2/evolution-chain/1/", result.EvolutionChain.Url);
    }

    [Fact]
    public async Task GetEvolutionChainAsync_ReturnsDeserializedChain()
    {
        const string url = "https://pokeapi.co/api/v2/evolution-chain/1/";
        var client = CreateClient(request =>
        {
            Assert.Equal(url, request.RequestUri!.ToString());
            return JsonResponse(HttpStatusCode.OK, """
                {
                  "id": 1,
                  "chain": {
                    "species": {"name": "bulbasaur", "url": "https://pokeapi.co/api/v2/pokemon-species/1/"},
                    "evolves_to": [
                      {
                        "species": {"name": "ivysaur", "url": "https://pokeapi.co/api/v2/pokemon-species/2/"},
                        "evolves_to": []
                      }
                    ]
                  }
                }
                """);
        }, out _);

        var result = await client.GetEvolutionChainAsync(url);

        Assert.Equal(1, result.Id);
        Assert.Equal("bulbasaur", result.Chain.Species.Name);
        Assert.Single(result.Chain.EvolvesTo);
        Assert.Equal("ivysaur", result.Chain.EvolvesTo[0].Species.Name);
    }
}
