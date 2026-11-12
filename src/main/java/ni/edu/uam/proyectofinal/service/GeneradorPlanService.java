package ni.edu.uam.proyectofinal.service;

import ni.edu.uam.proyectofinal.dao.PlanComidasDAO;
import ni.edu.uam.proyectofinal.dao.RecetaDAO;
import ni.edu.uam.proyectofinal.dao.RequerimientoDiarioDAO;
import ni.edu.uam.proyectofinal.model.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class GeneradorPlanService {

    private final RecetaDAO recetaDAO = new RecetaDAO();
    private final PlanComidasDAO planComidasDAO = new PlanComidasDAO();
    private final RequerimientoDiarioDAO requerimientoDAO = new RequerimientoDiarioDAO();
    private final CalculoNutricionalService calculoService = new CalculoNutricionalService();

    private static final List<String> DIAS_SEMANA = List.of(
            "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"
    );

    private static final Map<String, Double> DISTRIBUCION_COMIDAS = Map.of(
            "Desayuno", 0.25,
            "Almuerzo", 0.35,
            "Cena", 0.25,
            "Merienda", 0.15
    );

    public PlanComidas generarPlanSemanal(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Debe haber un usuario activo para generar el plan.");
        }

        List<Receta> todasLasRecetas = recetaDAO.listarTodas();
        if (todasLasRecetas.isEmpty()) {
            throw new IllegalStateException("No hay recetas registradas. Crea al menos una receta antes de generar el plan.");
        }

        RequerimientoDiario req = requerimientoDAO.buscarPorUsuario(usuario.getId());
        if (req == null && usuario.getPerfilFisico() != null) {
            req = calculoService.calcular(usuario);
            requerimientoDAO.guardar(req);
        }

        double metaCaloriasDiarias = (req != null && req.getCaloriasDiarias() != null)
                ? req.getCaloriasDiarias()
                : 2000.0;

        PlanComidas plan = new PlanComidas(
                usuario,
                req,
                "Plan Semanal (" + LocalDate.now() + ")",
                LocalDate.now(),
                metaCaloriasDiarias
        );

        List<String> tiempos = List.of("Desayuno", "Almuerzo", "Cena", "Merienda");

        for (int diaIdx = 0; diaIdx < DIAS_SEMANA.size(); diaIdx++) {
            String dia = DIAS_SEMANA.get(diaIdx);

            for (String tiempo : tiempos) {
                double objetivoComida = metaCaloriasDiarias * DISTRIBUCION_COMIDAS.get(tiempo);

                List<Receta> candidatas = todasLasRecetas.stream()
                        .filter(r -> r.getTipoComida() != null && r.getTipoComida().equalsIgnoreCase(tiempo))
                        .sorted(Comparator.comparingDouble(r -> Math.abs(r.getCaloriasTotales() - objetivoComida)))
                        .toList();

                if (candidatas.isEmpty()) {
                    candidatas = todasLasRecetas.stream()
                            .sorted(Comparator.comparingDouble(r -> Math.abs(r.getCaloriasTotales() - objetivoComida)))
                            .toList();
                }

                int indiceVariedad = diaIdx % Math.min(candidatas.size(), 3);
                Receta elegida = candidatas.get(indiceVariedad);

                plan.agregarComida(new ComidaPlan(elegida, dia, tiempo));
            }
        }

        planComidasDAO.guardar(plan);
        return plan;
    }
}