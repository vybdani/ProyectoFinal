package ni.edu.uam.proyectofinal.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ni.edu.uam.proyectofinal.model.AlimentoExterno;
import ni.edu.uam.proyectofinal.model.ConsejoNutricional;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Cliente HTTP para consumir la API REST de NutriFit (Spring Boot)
 * y la API de la red (Open Food Facts) desde JavaFX.
 */
public class NutricionApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient httpClient;
    private final Gson gson;

    public NutricionApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(4))
                .build();
        this.gson = new Gson();
    }

    /**
     * Verifica si el servidor backend Spring Boot está encendido.
     */
    public CompletableFuture<Boolean> verificarEstadoApiAsync() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/status"))
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> res.statusCode() == 200)
                .exceptionally(ex -> false);
    }

    /**
     * Consulta alimentos en internet (Open Food Facts a través de nuestra API).
     */
    public CompletableFuture<List<AlimentoExterno>> buscarAlimentosAsync(String criterio) {
        String query = URLEncoder.encode(criterio, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/alimentos/buscar?q=" + query))
                .timeout(Duration.ofSeconds(6))
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        Type listType = new TypeToken<List<AlimentoExterno>>() {}.getType();
                        return gson.fromJson(response.body(), listType);
                    }
                    return Collections.<AlimentoExterno>emptyList();
                })
                .exceptionally(ex -> Collections.emptyList());
    }

    /**
     * Obtiene recomendaciones y consejos según el objetivo (volumen, pérdida de grasa, etc.).
     */
    public CompletableFuture<ConsejoNutricional> obtenerConsejoAsync(String objetivo) {
        String objParam = URLEncoder.encode(objetivo != null ? objetivo : "mantener", StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/consejos?objetivo=" + objParam))
                .timeout(Duration.ofSeconds(5))
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return gson.fromJson(response.body(), ConsejoNutricional.class);
                    }
                    return crearConsejoRespaldo(objetivo);
                })
                .exceptionally(ex -> crearConsejoRespaldo(objetivo));
    }

    private ConsejoNutricional crearConsejoRespaldo(String objetivo) {
        return new ConsejoNutricional(
                objetivo != null ? objetivo : "Salud General",
                "Pauta Nutricional Sugerida",
                "Consume fuentes de proteína magra en cada comida, mantén hidratación constante (2L+ al día) y adecúa tus carbohidratos a tu actividad física.",
                "50% Carbohidratos | 25% Proteínas | 25% Grasas",
                "35 ml por kg de peso corporal",
                "NutriFit Modo Local (Inicia la API en puerto 8080 para datos en vivo)"
        );
    }
}
