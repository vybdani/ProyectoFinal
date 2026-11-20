package ni.edu.uam.api.service;

import ni.edu.uam.api.dto.CalculoMacrosRequest;
import ni.edu.uam.api.dto.CalculoMacrosResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculoMacrosServiceTest {

    private final CalculoMacrosService servicio = new CalculoMacrosService();

    @Test
    void daElMismoResultadoQueLaAppDeEscritorio() {
        // Mismo caso probado en la app: mujer, 61 kg, 165 cm, 25 años, moderada, Definición -> 1680.5 kcal
        CalculoMacrosResponse r = servicio.calcular(
                new CalculoMacrosRequest(61.0, 165.0, 25, "Femenino", "Moderado", "Definición", null));
        assertEquals("Mifflin-St Jeor", r.formula());
        assertEquals(1680.5, r.caloriasObjetivo());
        assertEquals(134.2, r.proteinasG());
        assertEquals(46.7, r.grasasG());
    }

    @Test
    void usaKatchMcArdleSiHayPorcentajeDeGrasa() {
        CalculoMacrosResponse r = servicio.calcular(
                new CalculoMacrosRequest(80.0, 180.0, 30, "Masculino", "Alto", "volumen", 15.0));
        assertEquals("Katch-McArdle", r.formula());
        assertEquals(1838.8, r.tmb());                       // 370 + 21.6 * 68
        assertEquals(Math.round(1838.8 * 1.725 * 1.12 * 10) / 10.0, r.caloriasObjetivo(), 0.2);
        assertEquals(160.0, r.proteinasG());                 // 2.0 g/kg
    }

    @Test
    void respetaElMinimoSeguroDeCalorias() {
        CalculoMacrosResponse r = servicio.calcular(
                new CalculoMacrosRequest(40.0, 150.0, 70, "Femenino", "Sedentario", "Bajar de peso", null));
        assertEquals(1200.0, r.caloriasObjetivo());
    }

    @Test
    void objetivoVacioEsMantener() {
        CalculoMacrosResponse r = servicio.calcular(
                new CalculoMacrosRequest(70.0, 175.0, 25, "Masculino", "Ligero", "", null));
        assertEquals("Mantener peso", r.objetivo());
        assertEquals(r.caloriasMantenimiento(), r.caloriasObjetivo());
    }

    @Test
    void rechazaUnObjetivoQueNoExiste() {
        assertThrows(IllegalArgumentException.class, () -> servicio.calcular(
                new CalculoMacrosRequest(70.0, 175.0, 25, "Masculino", "Ligero", "ser astronauta", null)));
    }
}
