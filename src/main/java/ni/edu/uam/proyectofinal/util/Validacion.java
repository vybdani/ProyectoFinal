package ni.edu.uam.proyectofinal.util;

import javafx.css.PseudoClass;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Control;
import javafx.scene.control.TextInputControl;

/**
 * Lectura y validación de los campos de los formularios.
 *
 * <p>Cada método devuelve el valor ya convertido o lanza {@link ValidacionException}
 * con un mensaje para el usuario. Además marca el campo con la pseudo-clase {@code :error}
 * (borde rojo, ver app.css), le pone el foco y quita la marca en cuanto el usuario lo corrige.</p>
 */
public final class Validacion {

    public static final String PATRON_CORREO = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$";

    private static final PseudoClass ERROR = PseudoClass.getPseudoClass("error");
    private static final String ESCUCHA_INSTALADA = "validacion.escucha";

    private Validacion() {
    }

    /** Dato del formulario que no cumple una regla. */
    public static class ValidacionException extends RuntimeException {
        public ValidacionException(String mensaje) {
            super(mensaje);
        }
    }

    // ------------------------------------------------------------- Texto

    /** Texto obligatorio, sin espacios a los lados y con largo máximo. */
    public static String texto(TextInputControl campo, String nombre, int maximo) {
        String valor = campo.getText() == null ? "" : campo.getText().trim();
        if (valor.isEmpty()) {
            throw fallo(campo, "Escribe " + nombre + ".");
        }
        if (valor.length() > maximo) {
            throw fallo(campo, capitalizar(nombre) + " admite como máximo " + maximo + " caracteres.");
        }
        return valor;
    }

    /** Texto opcional: devuelve "" si está vacío. */
    public static String textoOpcional(TextInputControl campo, String nombre, int maximo) {
        String valor = campo.getText() == null ? "" : campo.getText().trim();
        if (valor.length() > maximo) {
            throw fallo(campo, capitalizar(nombre) + " admite como máximo " + maximo + " caracteres.");
        }
        return valor;
    }

    /** Correo con formato válido (nombre@dominio.ext). */
    public static String correo(TextInputControl campo) {
        String valor = texto(campo, "tu correo", 100);
        if (!valor.matches(PATRON_CORREO)) {
            throw fallo(campo, "El correo no tiene un formato válido. Ejemplo: ana@correo.com");
        }
        return valor;
    }

    // ----------------------------------------------------------- Números

    /** Número decimal obligatorio dentro de [mínimo, máximo]. Acepta coma o punto decimal. */
    public static double decimal(TextInputControl campo, String nombre, double minimo, double maximo) {
        Double valor = decimalOpcional(campo, nombre, minimo, maximo);
        if (valor == null) {
            throw fallo(campo, "Escribe " + nombre + ".");
        }
        return valor;
    }

    /** Número decimal opcional: devuelve null si el campo está vacío. */
    public static Double decimalOpcional(TextInputControl campo, String nombre, double minimo, double maximo) {
        String texto = campo.getText() == null ? "" : campo.getText().trim().replace(',', '.');
        if (texto.isEmpty()) {
            return null;
        }
        double valor;
        try {
            valor = Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            throw fallo(campo, capitalizar(nombre) + " debe ser un número. Ejemplo: 65.5");
        }
        // parseDouble acepta "NaN" e "Infinity": se rechazan explícitamente
        if (Double.isNaN(valor) || Double.isInfinite(valor)) {
            throw fallo(campo, capitalizar(nombre) + " debe ser un número. Ejemplo: 65.5");
        }
        if (valor < minimo || valor > maximo) {
            throw fallo(campo, capitalizar(nombre) + " debe estar entre " + formato(minimo) + " y " + formato(maximo) + ".");
        }
        return valor;
    }

    /** Número entero obligatorio dentro de [mínimo, máximo]. */
    public static int entero(TextInputControl campo, String nombre, int minimo, int maximo) {
        Integer valor = enteroOpcional(campo, nombre, minimo, maximo);
        if (valor == null) {
            throw fallo(campo, "Escribe " + nombre + ".");
        }
        return valor;
    }

    /** Número entero opcional: devuelve null si el campo está vacío. */
    public static Integer enteroOpcional(TextInputControl campo, String nombre, int minimo, int maximo) {
        String texto = campo.getText() == null ? "" : campo.getText().trim();
        if (texto.isEmpty()) {
            return null;
        }
        int valor;
        try {
            valor = Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw fallo(campo, capitalizar(nombre) + " debe ser un número entero, sin decimales.");
        }
        if (valor < minimo || valor > maximo) {
            throw fallo(campo, capitalizar(nombre) + " debe estar entre " + minimo + " y " + maximo + ".");
        }
        return valor;
    }

    // ---------------------------------------------------------- Listas

    /** Valor elegido en un ComboBox o DatePicker; obligatorio. */
    public static <T> T seleccion(ComboBoxBase<T> campo, String nombre) {
        T valor = campo.getValue();
        if (valor == null) {
            throw fallo(campo, "Selecciona " + nombre + ".");
        }
        return valor;
    }

    /** Lanza un error de validación asociado a un campo (para reglas propias de una pantalla). */
    public static ValidacionException fallo(Control campo, String mensaje) {
        marcar(campo);
        return new ValidacionException(mensaje);
    }

    // ---------------------------------------------------------- Interno

    private static void marcar(Control campo) {
        if (campo == null) {
            return;
        }
        campo.pseudoClassStateChanged(ERROR, true);
        campo.requestFocus();
        if (campo.getProperties().putIfAbsent(ESCUCHA_INSTALADA, Boolean.TRUE) == null) {
            Runnable limpiar = () -> campo.pseudoClassStateChanged(ERROR, false);
            if (campo instanceof TextInputControl t) {
                t.textProperty().addListener((obs, antes, ahora) -> limpiar.run());
            } else if (campo instanceof ComboBoxBase<?> c) {
                c.valueProperty().addListener((obs, antes, ahora) -> limpiar.run());
            }
        }
    }

    private static String formato(double valor) {
        return valor == Math.rint(valor) ? String.valueOf((long) valor) : String.valueOf(valor);
    }

    /** "el peso" -> "El peso" (para empezar la oración del mensaje). */
    private static String capitalizar(String texto) {
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
