using BallastLane.AgenticAi.Api.PokeApi.Clients;
using BallastLane.AgenticAi.Api.PokeApi.Dtos;
using BallastLane.AgenticAi.Api.Pokemon.Services;
using Moq;

namespace BallastLane.AgenticAi.Api.Tests.Pokemon.Services;

public class PokemonServiceTests
{
    private readonly Mock<IPokeApiClient> _pokeApiClient = new();
    private readonly PokemonService _service;

    public PokemonServiceTests()
    {
        _service = new PokemonService(_pokeApiClient.Object);
    }

    private static PokemonDto CreatePokemonDto(long id, string name, string? sprite, int weight, params string[] abilities)
    {
        return new PokemonDto(
            id,
            name,
            Height: 7,
            Weight: weight,
            BaseExperience: 64,
            Sprites: new PokemonSpritesDto(sprite, null),
            Abilities: abilities
                .Select(a => new PokemonAbilityDto(new NamedApiResourceDto(a, $"https://pokeapi.co/api/v2/ability/{a}/"), false, 1))
                .ToList(),
            Stats: [],
            Types: [],
            Species: new NamedApiResourceDto(name, $"https://pokeapi.co/api/v2/pokemon-species/{id}/"));
    }

    private static PokemonSpeciesDto CreateSpeciesDto(long id, string name, string? englishGenus)
    {
        var genera = englishGenus is null
            ? []
            : new List<GenusDto> { new(englishGenus, new NamedApiResourceDto("en", "https://pokeapi.co/api/v2/language/9/")) };
        return new PokemonSpeciesDto(id, name, genera, [], new NamedApiResourceDto(null!, $"https://pokeapi.co/api/v2/evolution-chain/{id}/"));
    }

    [Fact]
    public async Task ListPokemonAsync_CombinesListPokemonAndSpeciesIntoSummaries()
    {
        _pokeApiClient
            .Setup(c => c.ListPokemonAsync(20, 0, It.IsAny<CancellationToken>()))
            .ReturnsAsync(new PokemonListResponseDto(1302, null, null,
                [new NamedApiResourceDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/")]));
        _pokeApiClient
            .Setup(c => c.GetPokemonAsync("bulbasaur", It.IsAny<CancellationToken>()))
            .ReturnsAsync(CreatePokemonDto(1, "bulbasaur", "https://example.com/sprite.png", 69, "overgrow", "chlorophyll"));
        _pokeApiClient
            .Setup(c => c.GetPokemonSpeciesAsync("bulbasaur", It.IsAny<CancellationToken>()))
            .ReturnsAsync(CreateSpeciesDto(1, "bulbasaur", "Seed Pokémon"));

        var result = await _service.ListPokemonAsync(1, 20);

        Assert.Equal(1, result.Page);
        Assert.Equal(20, result.Size);
        Assert.Equal(1302, result.TotalCount);
        Assert.Single(result.Items);

        var summary = result.Items[0];
        Assert.Equal(1, summary.Id);
        Assert.Equal("bulbasaur", summary.Name);
        Assert.Equal("https://example.com/sprite.png", summary.Sprite);
        Assert.Equal("Seed Pokémon", summary.Category);
        Assert.Equal(69, summary.Mass);
        Assert.Equal(["overgrow", "chlorophyll"], summary.Skills);
    }

    [Fact]
    public async Task ListPokemonAsync_ComputesOffsetFromPageAndSize()
    {
        _pokeApiClient
            .Setup(c => c.ListPokemonAsync(10, 20, It.IsAny<CancellationToken>()))
            .ReturnsAsync(new PokemonListResponseDto(0, null, null, []));

        await _service.ListPokemonAsync(3, 10);

        _pokeApiClient.Verify(c => c.ListPokemonAsync(10, 20, It.IsAny<CancellationToken>()), Times.Once);
    }

    [Fact]
    public async Task ListPokemonAsync_UsesNullSprite_WhenSpritesMissing()
    {
        _pokeApiClient
            .Setup(c => c.ListPokemonAsync(20, 0, It.IsAny<CancellationToken>()))
            .ReturnsAsync(new PokemonListResponseDto(1, null, null,
                [new NamedApiResourceDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/")]));
        _pokeApiClient
            .Setup(c => c.GetPokemonAsync("bulbasaur", It.IsAny<CancellationToken>()))
            .ReturnsAsync(new PokemonDto(1, "bulbasaur", 7, 69, 64, Sprites: null!, Abilities: [], Stats: [], Types: [],
                Species: new NamedApiResourceDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon-species/1/")));
        _pokeApiClient
            .Setup(c => c.GetPokemonSpeciesAsync("bulbasaur", It.IsAny<CancellationToken>()))
            .ReturnsAsync(CreateSpeciesDto(1, "bulbasaur", null));

        var result = await _service.ListPokemonAsync(1, 20);

        Assert.Null(result.Items[0].Sprite);
        Assert.Null(result.Items[0].Category);
    }
}
