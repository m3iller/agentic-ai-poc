using BallastLane.AgenticAi.Api.PokeApi.Clients;
using BallastLane.AgenticAi.Api.PokeApi.Dtos;

namespace BallastLane.AgenticAi.Api.Pokemon.Services;

/// <summary>
/// <see cref="IPokemonService"/> implementation that calls <see cref="IPokeApiClient"/> directly
/// per request and combines the pokemon-list, pokemon, and pokemon-species PokeAPI resources into
/// <see cref="PokemonSummary"/> entries. There is no local persistence/caching yet (see
/// pokemon-data-synchronization for that) — every call fans out to PokeAPI.
/// </summary>
public sealed class PokemonService : IPokemonService
{
    private const string EnglishLanguage = "en";

    private readonly IPokeApiClient _pokeApiClient;

    public PokemonService(IPokeApiClient pokeApiClient)
    {
        _pokeApiClient = pokeApiClient;
    }

    public async Task<PokemonPage> ListPokemonAsync(int page, int size, CancellationToken cancellationToken = default)
    {
        var offset = (page - 1) * size;
        var listResponse = await _pokeApiClient.ListPokemonAsync(size, offset, cancellationToken);

        var items = new List<PokemonSummary>(listResponse.Results.Count);
        foreach (var reference in listResponse.Results)
        {
            items.Add(await ToSummaryAsync(reference, cancellationToken));
        }

        return new PokemonPage(page, size, listResponse.Count, items);
    }

    private async Task<PokemonSummary> ToSummaryAsync(NamedApiResourceDto reference, CancellationToken cancellationToken)
    {
        var pokemon = await _pokeApiClient.GetPokemonAsync(reference.Name, cancellationToken);
        var species = await _pokeApiClient.GetPokemonSpeciesAsync(reference.Name, cancellationToken);

        var skills = pokemon.Abilities.Select(ability => ability.Ability.Name).ToList();

        return new PokemonSummary(
            pokemon.Id,
            pokemon.Name,
            pokemon.Sprites?.FrontDefault,
            EnglishGenus(species),
            pokemon.Weight,
            skills);
    }

    private static string? EnglishGenus(PokemonSpeciesDto species)
    {
        return species.Genera?
            .FirstOrDefault(genus => EnglishLanguage.Equals(genus.Language.Name, StringComparison.Ordinal))
            ?.Genus;
    }
}
