package ni.edu.uam.proyectofinal.reporte;

/** No se pudo generar un reporte; el mensaje está pensado para mostrarse al usuario. */
public class ReporteException extends RuntimeException {

    public ReporteException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
