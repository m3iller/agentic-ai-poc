namespace BallastLane.AgenticAi.Api.Pokemon.Services;

/// <summary>
/// Owns the business rules for browsing Pokemon (User Story 01). Speaks only domain types
/// (<see cref="PokemonPage"/>/<see cref="PokemonSummary"/>) — never HTTP or DTO types, per this
/// project's layering conventions. Depends on the pokeapi-integration client, never on a
/// Controller or DTO.
/// </summary>
public interface IPokemonService
{
    /// <summary>
    /// Fetches one page of Pokemon, each including sprite, category, mass, and skills.
    /// </summary>
    /// <param name="page">1-based page number</param>
    /// <param name="size">number of entries per page</param>
    Task<PokemonPage> ListPokemonAsync(int page, int size, CancellationToken cancellationToken = default);
}
