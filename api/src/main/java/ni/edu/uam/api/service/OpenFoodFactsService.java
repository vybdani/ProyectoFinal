package ni.edu.uam.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ni.edu.uam.api.dto.AlimentoExternoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Busca alimentos y sus macronutrientes (por 100 g) en Open Food Facts.
 *
 * <ul>
 *   <li>Usa el buscador oficial search.openfoodfacts.org.</li>
 *   <li>Descarta productos sin datos nutricionales completos.</li>
 *   <li>Guarda los resultados en memoria unos minutos para no repetir la misma petición.</li>
 *   <li>Si no hay internet o el servicio falla, responde con un catálogo local de respaldo.</li>
 * </ul>
 */
@Service
public class OpenFoodFactsService {

    public static final String FUENTE_RED = "Open Food Facts";
    public static final String FUENTE_LOCAL = "Catálogo local de Solea (sin conexión)";

    private static final Logger log = LoggerFactory.getLogger(OpenFoodFactsService.class);
    private static final double KJ_POR_KCAL = 4.184;
    private static final String CAMPOS =
            "code,product_name,product_name_es,brands,categories_tags,image_front_small_url,nutriments";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String url;
    private final Duration duracionCache;
    private final Map<String, EntradaCache> cache = new ConcurrentHashMap<>();

    private record EntradaCache(List<AlimentoExternoDTO> alimentos, Instant vence) {
    }

    public OpenFoodFactsService(RestTemplate restTemplate, ObjectMapper objectMapper,
                                @Value("${app.openfoodfacts.url}") String url,
                                @Value("${app.openfoodfacts.cache-minutos:30}") long minutosCache) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.url = url;
        this.duracionCache = Duration.ofMinutes(minutosCache);
    }

    /**
     * @param criterio nombre del alimento, por ejemplo "avena" (ya validado por el controlador)
     * @param limite   máximo de resultados
     */
    public List<AlimentoExternoDTO> buscarAlimentos(String criterio, int limite) {
        String clave = normalizar(criterio) + "|" + limite;
        EntradaCache guardada = cache.get(clave);
        if (guardada != null && guardada.vence().isAfter(Instant.now())) {
            return guardada.alimentos();
        }
        try {
            URI uri = UriComponentsBuilder.fromUriString(url)
                    .queryParam("q", criterio.trim())
                    .queryParam("langs", "es")
                    .queryParam("page_size", Math.min(limite * 2, 40)) // se piden de más porque se filtran
                    .queryParam("fields", CAMPOS)
                    .encode()
                    .build()
                    .toUri();
            String respuesta = restTemplate.getForObject(uri, String.class);
            List<AlimentoExternoDTO> alimentos = interpretarRespuesta(respuesta, limite);
            cache.put(clave, new EntradaCache(alimentos, Instant.now().plus(duracionCache)));
            return alimentos;
        } catch (Exception e) {
            log.warn("Open Food Facts no respondió ({}). Se usa el catálogo local.", e.getMessage());
            return catalogoLocal(criterio, limite);
        }
    }

    /** Convierte el JSON de Open Food Facts en la lista de alimentos (público para poder probarlo). */
    public List<AlimentoExternoDTO> interpretarRespuesta(String json, int limite) throws Exception {
        List<AlimentoExternoDTO> resultado = new ArrayList<>();
        if (json == null || json.isBlank()) {
            return resultado;
        }
        JsonNode productos = objectMapper.readTree(json).path("hits");
        for (JsonNode p : productos) {
            AlimentoExternoDTO alimento = convertir(p);
            if (alimento != null) {
                resultado.add(alimento);
                if (resultado.size() >= limite) {
                    break;
                }
            }
        }
        return resultado;
    }

    /** Devuelve null si al producto le falta el nombre o algún dato nutricional. */
    private AlimentoExternoDTO convertir(JsonNode p) {
        String nombre = texto(p, "product_name_es");
        if (nombre == null) {
            nombre = texto(p, "product_name");
        }
        JsonNode n = p.path("nutriments");
        Double kcal = numero(n, "energy-kcal_100g");
        if (kcal == null) {
            Double kj = numero(n, "energy-kj_100g");
            kcal = kj != null ? kj / KJ_POR_KCAL : null;
        }
        Double proteinas = numero(n, "proteins_100g");
        Double carbohidratos = numero(n, "carbohydrates_100g");
        Double grasas = numero(n, "fat_100g");
        if (nombre == null || kcal == null || proteinas == null || carbohidratos == null || grasas == null
                || kcal > 900 || proteinas + carbohidratos + grasas > 100.5) {
            return null; // datos incompletos o imposibles por 100 g
        }
        return AlimentoExternoDTO.builder()
                .id(texto(p, "code"))
                .nombre(nombre.length() > 100 ? nombre.substring(0, 100) : nombre)
                .marca(primero(p.path("brands"), "Genérico"))
                .caloriasPor100g(redondear(kcal))
                .proteinasPor100g(redondear(proteinas))
                .carbohidratosPor100g(redondear(carbohidratos))
                .grasasPor100g(redondear(grasas))
                .categoria(categoria(p.path("categories_tags")))
                .imagenUrl(texto(p, "image_front_small_url"))
                .fuente(FUENTE_RED)
                .build();
    }

    /** Alimentos de respaldo para cuando no hay internet (por ejemplo durante una presentación). */
    List<AlimentoExternoDTO> catalogoLocal(String criterio, int limite) {
        List<AlimentoExternoDTO> catalogo = List.of(
                local("1", "Pechuga de pollo", 165.0, 31.0, 0.0, 3.6, "Carnes"),
                local("2", "Avena integral", 389.0, 16.9, 66.3, 6.9, "Cereales"),
                local("3", "Huevo entero", 155.0, 13.0, 1.1, 11.0, "Huevos"),
                local("4", "Arroz blanco cocido", 130.0, 2.7, 28.2, 0.3, "Cereales"),
                local("5", "Atún al natural", 116.0, 26.0, 0.0, 1.0, "Pescados"),
                local("6", "Plátano maduro", 89.0, 1.1, 22.8, 0.3, "Frutas"),
                local("7", "Yogur griego natural", 59.0, 10.0, 3.6, 0.4, "Lácteos"),
                local("8", "Frijoles negros cocidos", 132.0, 8.9, 23.7, 0.5, "Legumbres"),
                local("9", "Aguacate", 160.0, 2.0, 8.5, 14.7, "Frutas"),
                local("10", "Leche descremada", 34.0, 3.4, 5.0, 0.1, "Lácteos"));
        String busqueda = normalizar(criterio);
        return catalogo.stream()
                .filter(a -> normalizar(a.getNombre()).contains(busqueda))
                .limit(limite)
                .toList();
    }

    private static AlimentoExternoDTO local(String id, String nombre, double kcal, double p, double c, double g,
                                            String categoria) {
        return new AlimentoExternoDTO(id, nombre, "Solea", kcal, p, c, g, categoria, null, FUENTE_LOCAL);
    }

    private static String texto(JsonNode nodo, String campo) {
        JsonNode valor = nodo.path(campo);
        return valor.isTextual() && !valor.asText().isBlank() ? valor.asText().trim() : null;
    }

    private static Double numero(JsonNode nodo, String campo) {
        JsonNode valor = nodo.path(campo);
        return valor.isNumber() && valor.asDouble() >= 0 ? valor.asDouble() : null;
    }

    /** "brands" llega como lista ["Hacendado"] o como texto "Hacendado, Otra". */
    private static String primero(JsonNode nodo, String porDefecto) {
        if (nodo.isArray() && !nodo.isEmpty()) {
            return nodo.get(0).asText();
        }
        if (nodo.isTextual() && !nodo.asText().isBlank()) {
            return nodo.asText().split(",")[0].trim();
        }
        return porDefecto;
    }

    /** "en:breakfast-cereals" -> "Breakfast cereals" (la categoría más específica es la última). */
    private static String categoria(JsonNode etiquetas) {
        if (!etiquetas.isArray() || etiquetas.isEmpty()) {
            return "General";
        }
        String etiqueta = etiquetas.get(etiquetas.size() - 1).asText();
        String sinIdioma = etiqueta.contains(":") ? etiqueta.substring(etiqueta.indexOf(':') + 1) : etiqueta;
        String legible = sinIdioma.replace('-', ' ');
        return legible.isEmpty() ? "General" : Character.toUpperCase(legible.charAt(0)) + legible.substring(1);
    }

    private static double redondear(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }

    static String normalizar(String texto) {
        return Normalizer.normalize(texto == null ? "" : texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
