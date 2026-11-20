package ni.edu.uam.proyectofinal.service;

import ni.edu.uam.proyectofinal.dao.AlimentoDAO;
import ni.edu.uam.proyectofinal.dao.GrupoMuscularDAO;
import ni.edu.uam.proyectofinal.dao.RecetaDAO;

/**
 * Deja la base lista para el primer uso: alimentos comunes, recetas sugeridas y el catálogo de
 * ejercicios por grupo muscular. Solo agrega lo que falta, así que se puede llamar siempre.
 */
public class DatosInicialesService {

    private final AlimentoDAO alimentoDAO = new AlimentoDAO();
    private final RecetaDAO recetaDAO = new RecetaDAO();
    private final GrupoMuscularDAO grupoDAO = new GrupoMuscularDAO();

    /** Resumen de lo que se agregó en esta llamada. */
    public record Resultado(int alimentos, int recetas) {
    }

    public Resultado asegurar() {
        int alimentos = alimentoDAO.asegurarAlimentosIniciales();
        int recetas = recetaDAO.asegurarRecetasIniciales(); // necesita los alimentos de arriba
        grupoDAO.asegurarDatosIniciales();
        return new Resultado(alimentos, recetas);
    }
}
