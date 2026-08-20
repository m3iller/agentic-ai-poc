using BallastLane.AgenticAi.Api.Pokemon.Controllers;
using BallastLane.AgenticAi.Api.Pokemon.Services;
using Moq;

namespace BallastLane.AgenticAi.Api.Tests.Pokemon.Controllers;

public class PokemonControllerTests
{
    private readonly Mock<IPokemonService> _pokemonService = new();
    private readonly PokemonController _controller;

    public PokemonControllerTests()
    {
        _controller = new PokemonController(_pokemonService.Object);
    }

    [Fact]
    public async Task List_MapsServiceResultToResponseDto()
    {
        var page = new PokemonPage(
            1,
            20,
            1302,
            [new PokemonSummary(1, "bulbasaur", "https://example.com/sprite.png", "Seed Pokémon", 69, ["overgrow", "chlorophyll"])]);
        _pokemonService
            .Setup(s => s.ListPokemonAsync(1, 20, It.IsAny<CancellationToken>()))
            .ReturnsAsync(page);

        var result = await _controller.List(1, 20, CancellationToken.None);

        Assert.Equal(1, result.Page);
        Assert.Equal(20, result.Size);
        Assert.Equal(1302, result.TotalCount);
        Assert.Single(result.Items);

        var item = result.Items[0];
        Assert.Equal(1, item.Id);
        Assert.Equal("bulbasaur", item.Name);
        Assert.Equal("https://example.com/sprite.png", item.Sprite);
        Assert.Equal("Seed Pokémon", item.Category);
        Assert.Equal(69, item.Mass);
        Assert.Equal(["overgrow", "chlorophyll"], item.Skills);
    }

    [Fact]
    public async Task List_PassesPageAndSizeThroughToService()
    {
        _pokemonService
            .Setup(s => s.ListPokemonAsync(3, 10, It.IsAny<CancellationToken>()))
            .ReturnsAsync(new PokemonPage(3, 10, 0, []));

        await _controller.List(3, 10, CancellationToken.None);

        _pokemonService.Verify(s => s.ListPokemonAsync(3, 10, It.IsAny<CancellationToken>()), Times.Once);
    }
}
