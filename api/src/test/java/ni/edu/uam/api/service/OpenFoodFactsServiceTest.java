package ni.edu.uam.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ni.edu.uam.api.dto.AlimentoExternoDTO;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenFoodFactsServiceTest {

    /** Respuesta real recortada de search.openfoodfacts.org. */
    private static final String JSON = """
            {"hits":[
              {"code":"7501040003240","brands":["FUD"],
               "categories_tags":["en:meats","en:chicken-breasts"],
               "nutriments":{"carbohydrates_100g":2.72,"energy-kj_100g":354,"fat_100g":2.04,"proteins_100g":13.6},
               "product_name_es":"Pechuga de pollo con finas hierbas FUD"},
              {"code":"1","brands":["Sin datos"],"nutriments":{"energy-kcal_100g":50},"product_name":"Incompleto"},
              {"code":"8480000561565","brands":"Hacendado, Otra",
               "nutriments":{"carbohydrates_100g":1.3,"energy-kcal_100g":84,"fat_100g":1.2,"proteins_100g":16.9},
               "product_name":"Pechuga de pollo finas lonchas"}
            ]}""";

    private final OpenFoodFactsService servicio =
            new OpenFoodFactsService(new RestTemplate(), new ObjectMapper(), "http://localhost:1/sin-red", 30);

    @Test
    void interpretaLaRespuestaYDescartaProductosIncompletos() throws Exception {
        List<AlimentoExternoDTO> alimentos = servicio.interpretarRespuesta(JSON, 10);

        assertEquals(2, alimentos.size());
        AlimentoExternoDTO pollo = alimentos.get(0);
        assertEquals("Pechuga de pollo con finas hierbas FUD", pollo.getNombre());
        assertEquals("FUD", pollo.getMarca());
        assertEquals(84.6, pollo.getCaloriasPor100g());   // 354 kJ / 4.184 = 84.6 kcal
        assertEquals(13.6, pollo.getProteinasPor100g());
        assertEquals("Chicken breasts", pollo.getCategoria());
        assertEquals(OpenFoodFactsService.FUENTE_RED, pollo.getFuente());
        assertEquals("Hacendado", alimentos.get(1).getMarca()); // marca en texto separado por comas
    }

    @Test
    void respetaElLimite() throws Exception {
        assertEquals(1, servicio.interpretarRespuesta(JSON, 1).size());
    }

    @Test
    void sinConexionUsaElCatalogoLocal() {
        List<AlimentoExternoDTO> alimentos = servicio.buscarAlimentos("Atun", 5); // sin tilde también encuentra "Atún"
        assertFalse(alimentos.isEmpty());
        assertTrue(alimentos.stream().allMatch(a -> OpenFoodFactsService.FUENTE_LOCAL.equals(a.getFuente())));
        assertEquals("Atún al natural", alimentos.get(0).getNombre());
    }

    @Test
    void guardaLosResultadosEnCache() {
        int[] llamadas = {0};
        RestTemplate contador = new RestTemplate() {
            @Override
            public <T> T getForObject(URI url, Class<T> tipo) {
                llamadas[0]++;
                return tipo.cast(JSON);
            }
        };
        OpenFoodFactsService conCache = new OpenFoodFactsService(contador, new ObjectMapper(), "http://x/search", 30);
        conCache.buscarAlimentos("pollo", 5);
        conCache.buscarAlimentos("Pollo ", 5);
        assertEquals(1, llamadas[0]);
    }

    @Test
    void siOpenFoodFactsFallaNoLanzaError() {
        RestTemplate falla = new RestTemplate() {
            @Override
            public <T> T getForObject(URI url, Class<T> tipo) {
                throw new ResourceAccessException("timeout");
            }
        };
        OpenFoodFactsService conFallo = new OpenFoodFactsService(falla, new ObjectMapper(), "http://x/search", 30);
        assertFalse(conFallo.buscarAlimentos("avena", 5).isEmpty());
    }
}
