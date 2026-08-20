using System.ComponentModel.DataAnnotations;
using BallastLane.AgenticAi.Api.Pokemon.Dtos;
using BallastLane.AgenticAi.Api.Pokemon.Services;
using Microsoft.AspNetCore.Mvc;

namespace BallastLane.AgenticAi.Api.Pokemon.Controllers;

/// <summary>
/// HTTP-facing entry point for browsing Pokemon (pokemon-enumeration/US01). Maps query
/// parameters to an <see cref="IPokemonService"/> call and the resulting <see cref="PokemonPage"/>
/// (Model) to a <see cref="PokemonPageResponse"/> (View) — no business logic lives here.
/// Invalid <c>page</c>/<c>size</c> values are rejected with 400 automatically by
/// <see cref="ApiControllerAttribute"/>'s model-state validation (see the <see cref="RangeAttribute"/>
/// below), the .NET equivalent of the Java side's <c>@Min</c>/<c>@Max</c>.
/// </summary>
[ApiController]
[Route("api/pokemon")]
public sealed class PokemonController : ControllerBase
{
    private const int DefaultPage = 1;
    private const int DefaultSize = 20;
    private const int MaxSize = 100;

    private readonly IPokemonService _pokemonService;

    public PokemonController(IPokemonService pokemonService)
    {
        _pokemonService = pokemonService;
    }

    [HttpGet]
    public async Task<PokemonPageResponse> List(
        [FromQuery][Range(1, int.MaxValue)] int page = DefaultPage,
        [FromQuery][Range(1, MaxSize)] int size = DefaultSize,
        CancellationToken cancellationToken = default)
    {
        var result = await _pokemonService.ListPokemonAsync(page, size, cancellationToken);
        return ToResponse(result);
    }

    private static PokemonPageResponse ToResponse(PokemonPage page)
    {
        var items = page.Items.Select(ToResponse).ToList();
        return new PokemonPageResponse(page.Page, page.Size, page.TotalCount, items);
    }

    private static PokemonSummaryResponse ToResponse(PokemonSummary summary)
    {
        return new PokemonSummaryResponse(
            summary.Id,
            summary.Name,
            summary.Sprite,
            summary.Category,
            summary.Mass,
            summary.Skills);
    }
}
