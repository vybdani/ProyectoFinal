package ni.edu.uam.proyectofinal.service;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Cliente HTTP de la API REST de Solea (Spring Boot, carpeta /api), que a su vez consulta
 * Open Food Facts en internet.
 *
 * <p>Todas las llamadas son asíncronas para no congelar la ventana. La dirección se puede
 * cambiar con la variable de entorno {@code SOLEA_API_URL} (por defecto http://localhost:8081/api).</p>
 */
public class NutricionApiClient {

    private static final String URL_POR_DEFECTO = "http://localhost:8081/api";

    private final String baseUrl;
    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    public NutricionApiClient() {
        this(System.getenv().getOrDefault("SOLEA_API_URL", URL_POR_DEFECTO));
    }

    public NutricionApiClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    /** No se pudo usar la API: está apagada, no hay red o respondió con error. */
    public static class ApiNoDisponibleException extends RuntimeException {
        public ApiNoDisponibleException(String mensaje, Throwable causa) {
            super(mensaje, causa);
        }
    }

    /** true si la API de Solea está encendida. */
    public CompletableFuture<Boolean> verificarEstadoApiAsync() {
        return httpClient.sendAsync(peticion("/status", Duration.ofSeconds(3)), HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> res.statusCode() == 200)
                .exceptionally(ex -> false);
    }

    /**
     * Busca alimentos en internet (Open Food Facts a través de la API de Solea).
     * Si la API no responde, el futuro termina con {@link ApiNoDisponibleException}.
     */
    public CompletableFuture<List<AlimentoExterno>> buscarAlimentosAsync(String criterio, int limite) {
        String ruta = "/alimentos/buscar?q=" + URLEncoder.encode(criterio.trim(), StandardCharsets.UTF_8)
                + "&limite=" + limite;
        Type tipoLista = new TypeToken<List<AlimentoExterno>>() { }.getType();
        return httpClient.sendAsync(peticion(ruta, Duration.ofSeconds(12)), HttpResponse.BodyHandlers.ofString())
                .handle((respuesta, error) -> {
                    if (error != null) {
                        throw new ApiNoDisponibleException(
                                "La API de Solea no está encendida. Ejecútala (carpeta /api) para buscar alimentos en internet.", error);
                    }
                    if (respuesta.statusCode() != 200) {
                        throw new ApiNoDisponibleException(
                                "La API respondió con un error (" + respuesta.statusCode() + "). Revisa la búsqueda.", null);
                    }
                    try {
                        List<AlimentoExterno> alimentos = gson.fromJson(respuesta.body(), tipoLista);
                        return alimentos != null ? alimentos : List.<AlimentoExterno>of();
                    } catch (JsonParseException e) {
                        throw new ApiNoDisponibleException("La respuesta de la API no tiene el formato esperado.", e);
                    }
                });
    }

    /** Compatibilidad: búsqueda con el límite por defecto (8). */
    public CompletableFuture<List<AlimentoExterno>> buscarAlimentosAsync(String criterio) {
        return buscarAlimentosAsync(criterio, 8);
    }

    /**
     * Consejo nutricional para el objetivo (por ejemplo "Volumen muscular").
     * Si la API está apagada devuelve un consejo general local, nunca falla.
     */
    public CompletableFuture<ConsejoNutricional> obtenerConsejoAsync(String objetivo) {
        String objetivoUsado = objetivo != null && !objetivo.isBlank() ? objetivo : "Mantener peso";
        String ruta = "/consejos?objetivo=" + URLEncoder.encode(objetivoUsado, StandardCharsets.UTF_8);
        return httpClient.sendAsync(peticion(ruta, Duration.ofSeconds(5)), HttpResponse.BodyHandlers.ofString())
                .thenApply(respuesta -> respuesta.statusCode() == 200
                        ? gson.fromJson(respuesta.body(), ConsejoNutricional.class)
                        : consejoLocal(objetivoUsado))
                .exceptionally(ex -> consejoLocal(objetivoUsado));
    }

    private HttpRequest peticion(String ruta, Duration espera) {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + ruta))
                .timeout(espera)
                .header("Accept", "application/json")
                .GET()
                .build();
    }

    private ConsejoNutricional consejoLocal(String objetivo) {
        return new ConsejoNutricional(
                objetivo,
                "Pauta nutricional general",
                "Incluye proteína magra en cada comida, mantente hidratado (2 L o más al día) y ajusta "
                        + "los carbohidratos a tu actividad física.",
                "50% Carbohidratos | 25% Proteínas | 25% Grasas",
                "35 ml por kg de peso corporal",
                "Solea sin conexión (inicia la API en el puerto 8080 para recomendaciones en vivo)");
    }
}
