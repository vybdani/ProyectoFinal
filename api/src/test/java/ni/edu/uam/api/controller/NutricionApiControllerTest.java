package ni.edu.uam.api.controller;

import ni.edu.uam.api.dto.AlimentoExternoDTO;
import ni.edu.uam.api.service.CalculoMacrosService;
import ni.edu.uam.api.service.OpenFoodFactsService;
import ni.edu.uam.api.service.RecomendacionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NutricionApiController.class)
@Import({RecomendacionService.class, CalculoMacrosService.class})
class NutricionApiControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private OpenFoodFactsService openFoodFactsService;

    @Test
    void statusRespondeActivo() throws Exception {
        mvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    void buscarDevuelveLosAlimentos() throws Exception {
        when(openFoodFactsService.buscarAlimentos(anyString(), anyInt())).thenReturn(List.of(
                new AlimentoExternoDTO("1", "Avena", "Marca", 389.0, 16.9, 66.3, 6.9, "Cereales", null, "Open Food Facts")));
        mvc.perform(get("/api/alimentos/buscar").param("q", "avena"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Avena"))
                .andExpect(jsonPath("$[0].caloriasPor100g").value(389.0));
    }

    @Test
    void buscarConTextoMuyCortoDa400() throws Exception {
        mvc.perform(get("/api/alimentos/buscar").param("q", "a"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles", hasItem(containsString("entre 2 y 60"))));
    }

    @Test
    void buscarSinParametroDa400() throws Exception {
        mvc.perform(get("/api/alimentos/buscar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Falta un parámetro"));
    }

    @Test
    void consejoReconoceObjetivosConTilde() throws Exception {
        mvc.perform(get("/api/consejos").param("objetivo", "Definición"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.objetivo", containsString("Definición")));
    }

    @Test
    void calculoValidoDevuelveLasCalorias() throws Exception {
        mvc.perform(post("/api/calculo/macros").contentType(MediaType.APPLICATION_JSON).content("""
                        {"pesoKg":61,"alturaCm":165,"edad":25,"sexo":"Femenino",
                         "nivelActividad":"Moderado","objetivo":"Definición"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caloriasObjetivo").value(1680.5))
                .andExpect(jsonPath("$.proteinasG").value(134.2));
    }

    @Test
    void calculoConDatosInvalidosDa400ConLosMotivos() throws Exception {
        mvc.perform(post("/api/calculo/macros").contentType(MediaType.APPLICATION_JSON).content("""
                        {"pesoKg":-5,"alturaCm":165,"edad":12,"sexo":"Otro","nivelActividad":"Moderado"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles", hasItem("El peso debe estar entre 30 y 300 kg")))
                .andExpect(jsonPath("$.detalles", hasItem("La edad debe estar entre 18 y 100 años")))
                .andExpect(jsonPath("$.detalles", hasItem("El sexo debe ser Masculino o Femenino")));
    }

    @Test
    void calculoConObjetivoInexistenteDa400() throws Exception {
        mvc.perform(post("/api/calculo/macros").contentType(MediaType.APPLICATION_JSON).content("""
                        {"pesoKg":70,"alturaCm":175,"edad":25,"sexo":"Masculino",
                         "nivelActividad":"Ligero","objetivo":"ser astronauta"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles[0]", containsString("Objetivo no válido")));
    }

    @Test
    void jsonMalFormadoDa400() throws Exception {
        mvc.perform(post("/api/calculo/macros").contentType(MediaType.APPLICATION_JSON).content("{peso:"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("JSON inválido"));
    }
}
