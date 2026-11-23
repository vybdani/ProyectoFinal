package ni.edu.uam.proyectofinal.service;

import ni.edu.uam.proyectofinal.dao.AlimentoDAO;
import ni.edu.uam.proyectofinal.model.Alimento;
import ni.edu.uam.proyectofinal.model.AlimentoExterno;

import java.util.Locale;

/**
 * Convierte los alimentos que llegan de la API (Open Food Facts) en alimentos de Solea
 * y los guarda en PostgreSQL con Hibernate, para usarlos en recetas y en el registro de consumo.
 */
public class AlimentoService {

    private static final int MAX_NOMBRE = 100;
    private static final int MAX_CATEGORIA = 50;

    private final AlimentoDAO alimentoDAO = new AlimentoDAO();

    /**
     * Guarda el alimento de la API. Si ya existe uno con el mismo nombre, devuelve el existente
     * (no se duplica). Los valores de Open Food Facts son por 100 g, igual que en Solea.
     */
    public Alimento importar(AlimentoExterno externo) {
        validar(externo);
        String nombre = nombreLocal(externo);

        Alimento existente = alimentoDAO.buscarPorNombreExacto(nombre);
        if (existente != null) {
            return existente;
        }
        Alimento nuevo = new Alimento(
                nombre,
                categoriaLocal(externo.getCategoria()),
                externo.getCaloriasPor100g(),
                externo.getProteinasPor100g(),
                externo.getCarbohidratosPor100g(),
                externo.getGrasasPor100g());
        return alimentoDAO.guardar(nuevo);
    }

    /** Nombre con la marca entre paréntesis, por ejemplo "Avena en hojuelas (Quaker)". */
    static String nombreLocal(AlimentoExterno externo) {
        String nombre = externo.getNombre().trim().replaceAll("\\s+", " ");
        String marca = externo.getMarca();
        boolean agregarMarca = marca != null && !marca.isBlank()
                && !"Genérico".equalsIgnoreCase(marca) && !"Solea".equalsIgnoreCase(marca)
                && !nombre.toLowerCase(Locale.ROOT).contains(marca.toLowerCase(Locale.ROOT));
        String completo = agregarMarca ? nombre + " (" + marca.trim() + ")" : nombre;
        return completo.length() > MAX_NOMBRE ? completo.substring(0, MAX_NOMBRE).trim() : completo;
    }

    private static String categoriaLocal(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return "Importado";
        }
        String limpia = categoria.trim();
        return limpia.length() > MAX_CATEGORIA ? limpia.substring(0, MAX_CATEGORIA).trim() : limpia;
    }

    private static void validar(AlimentoExterno e) {
        if (e == null || e.getNombre() == null || e.getNombre().isBlank()) {
            throw new IllegalArgumentException("El alimento no tiene nombre.");
        }
        Double[] valores = {e.getCaloriasPor100g(), e.getProteinasPor100g(), e.getCarbohidratosPor100g(), e.getGrasasPor100g()};
        for (Double v : valores) {
            if (v == null || v < 0 || v.isNaN()) {
                throw new IllegalArgumentException("\"" + e.getNombre() + "\" no tiene información nutricional completa.");
            }
        }
        if (e.getCaloriasPor100g() > 900
                || e.getProteinasPor100g() + e.getCarbohidratosPor100g() + e.getGrasasPor100g() > 105.0) {
            throw new IllegalArgumentException("Los valores nutricionales de \"" + e.getNombre() + "\" no son válidos.");
        }
    }
}