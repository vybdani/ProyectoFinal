package ni.edu.uam.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ni.edu.uam.api.dto.AlimentoExternoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Servicio que interactúa con la API externa pública de Open Food Facts
 * para obtener información nutricional de alimentos en tiempo real desde internet.
 */
@Service
public class OpenFoodFactsService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.api.openfoodfacts.url:https://world.openfoodfacts.org/cgi/search.pl}")
    private String openFoodFactsUrl;

    public OpenFoodFactsService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Busca alimentos por nombre en la API mundial de Open Food Facts.
     *
     * @param criterio Término de búsqueda (ej. "avena", "yogurt", "leche", "arroz").
     * @return Lista de alimentos con sus macronutrientes por cada 100g.
     */
    public List<AlimentoExternoDTO> buscarAlimentos(String criterio) {
        if (criterio == null || criterio.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(openFoodFactsUrl)
                    .queryParam("search_terms", criterio.trim())
                    .queryParam("search_simple", "1")
                    .queryParam("action", "process")
                    .queryParam("json", "1")
                    .queryParam("page_size", "8")
                    .build()
                    .toUri();

            String response = restTemplate.getForObject(uri, String.class);
            if (response == null) {
                return obtenerAlimentosLocalesRespaldo(criterio);
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode products = root.path("products");

            List<AlimentoExternoDTO> resultados = new ArrayList<>();
            if (products.isArray()) {
                for (JsonNode product : products) {
                    String nombre = product.hasNonNull("product_name_es")
                            ? product.path("product_name_es").asText()
                            : product.path("product_name").asText("Alimento sin nombre");

                    if (nombre.isBlank()) continue;

                    JsonNode nutriments = product.path("nutriments");
                    double calorias = nutriments.hasNonNull("energy-kcal_100g")
                            ? nutriments.path("energy-kcal_100g").asDouble()
                            : nutriments.path("energy-kcal").asDouble(0.0);

                    double proteinas = nutriments.path("proteins_100g").asDouble(0.0);
                    double carbohidratos = nutriments.path("carbohydrates_100g").asDouble(0.0);
                    double grasas = nutriments.path("fat_100g").asDouble(0.0);

                    String marca = product.path("brands").asText("Genérico");
                    String categoria = product.path("categories").asText("General");
                    String imagen = product.path("image_front_small_url").asText(null);
                    String id = product.path("_id").asText(product.path("code").asText(""));

                    resultados.add(AlimentoExternoDTO.builder()
                            .id(id)
                            .nombre(nombre)
                            .marca(marca)
                            .caloriasPor100g(Math.round(calorias * 10.0) / 10.0)
                            .proteinasPor100g(Math.round(proteinas * 10.0) / 10.0)
                            .carbohidratosPor100g(Math.round(carbohidratos * 10.0) / 10.0)
                            .grasasPor100g(Math.round(grasas * 10.0) / 10.0)
                            .categoria(categoria)
                            .imagenUrl(imagen)
                            .fuente("Open Food Facts API (Red)")
                            .build());
                }
            }

            return resultados.isEmpty() ? obtenerAlimentosLocalesRespaldo(criterio) : resultados;

        } catch (Exception e) {
            System.err.println("Aviso: No se pudo conectar a Open Food Facts (" + e.getMessage() + "), usando respaldo local.");
            return obtenerAlimentosLocalesRespaldo(criterio);
        }
    }

    /**
     * Alimentos de respaldo en caso de falta de conexión a internet durante una presentación.
     */
    private List<AlimentoExternoDTO> obtenerAlimentosLocalesRespaldo(String criterio) {
        List<AlimentoExternoDTO> catalogo = List.of(
                new AlimentoExternoDTO("1", "Pechuga de Pollo", "NutriFit", 165.0, 31.0, 0.0, 3.6, "Carnes", null, "NutriFit Base"),
                new AlimentoExternoDTO("2", "Avena Integral", "NutriFit", 389.0, 16.9, 66.3, 6.9, "Cereales", null, "NutriFit Base"),
                new AlimentoExternoDTO("3", "Huevo Entero", "NutriFit", 155.0, 13.0, 1.1, 11.0, "Huevos", null, "NutriFit Base"),
                new AlimentoExternoDTO("4", "Arroz Blanco Cocido", "NutriFit", 130.0, 2.7, 28.2, 0.3, "Cereales", null, "NutriFit Base"),
                new AlimentoExternoDTO("5", "Atún al Natural", "NutriFit", 116.0, 26.0, 0.0, 1.0, "Pescados", null, "NutriFit Base"),
                new AlimentoExternoDTO("6", "Plátano Maduro", "NutriFit", 89.0, 1.1, 22.8, 0.3, "Frutas", null, "NutriFit Base")
        );

        String busqueda = criterio.toLowerCase();
        return catalogo.stream()
                .filter(a -> a.getNombre().toLowerCase().contains(busqueda))
                .toList();
    }
}
