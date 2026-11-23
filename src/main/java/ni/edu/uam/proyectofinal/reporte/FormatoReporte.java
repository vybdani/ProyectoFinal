package ni.edu.uam.proyectofinal.reporte;

import java.nio.file.Path;
import java.util.Locale;

/** Formatos a los que se puede exportar un reporte de Solea. */
public enum FormatoReporte {

    PDF("pdf", "PDF", "Documento PDF"),
    EXCEL("xlsx", "Excel", "Libro de Excel");

    private final String extension;
    private final String nombre;
    private final String descripcion;

    FormatoReporte(String extension, String nombre, String descripcion) {
        this.extension = extension;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    /** Extensión sin punto, por ejemplo "xlsx". */
    public String getExtension() {
        return extension;
    }

    /** Nombre corto para mostrar, por ejemplo "Excel". */
    public String getNombre() {
        return nombre;
    }

    /** Descripción para el filtro del selector de archivos. */
    public String getDescripcion() {
        return descripcion;
    }

    /** Deduce el formato por la extensión del archivo (si no es .xlsx, se toma como PDF). */
    public static FormatoReporte deArchivo(Path archivo) {
        String nombreArchivo = archivo.getFileName().toString().toLowerCase(Locale.ROOT);
        return nombreArchivo.endsWith("." + EXCEL.extension) ? EXCEL : PDF;
    }

    /** Devuelve la ruta con la extensión de este formato (la agrega si falta). */
    public Path conExtension(Path archivo) {
        String nombreArchivo = archivo.getFileName().toString();
        return nombreArchivo.toLowerCase(Locale.ROOT).endsWith("." + extension)
                ? archivo
                : archivo.resolveSibling(nombreArchivo + "." + extension);
    }
}
