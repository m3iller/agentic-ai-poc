using BallastLane.AgenticAi.Api.Common.Errors;
using BallastLane.AgenticAi.Api.PokeApi.Clients;
using BallastLane.AgenticAi.Api.Pokemon.Services;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.

builder.Services.AddControllers();
// Learn more about configuring OpenAPI at https://aka.ms/aspnet/openapi
builder.Services.AddOpenApi();

builder.Services.AddHttpClient<IPokeApiClient, PokeApiClient>((sp, client) =>
{
    var configuredBaseUrl = sp.GetRequiredService<IConfiguration>()["PokeApi:BaseUrl"]
        ?? "https://pokeapi.co/api/v2/";
    // HttpClient combines BaseAddress + a relative request URI per standard URI-combination
    // rules, which only works correctly if BaseAddress ends with a trailing slash.
    var baseUrl = configuredBaseUrl.EndsWith('/') ? configuredBaseUrl : configuredBaseUrl + "/";
    client.BaseAddress = new Uri(baseUrl);
});

builder.Services.AddScoped<IPokemonService, PokemonService>();

builder.Services.AddExceptionHandler<PokeApiExceptionHandler>();
builder.Services.AddProblemDetails();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}

app.UseExceptionHandler();

app.UseHttpsRedirection();

app.UseAuthorization();

app.MapControllers();

app.Run();

// Exposes the top-level-statement-generated Program class to AgenticAi.Api.Tests so it can be
// used as WebApplicationFactory<Program>'s entry point for integration tests.
public partial class Program;
