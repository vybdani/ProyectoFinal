package ni.edu.uam.proyectofinal.util;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

import java.text.Normalizer;
import java.util.Collection;
import java.util.Locale;
import java.util.function.Function;

/**
 * Convierte un ComboBox en un buscador: el usuario escribe parte del nombre y la lista
 * muestra solo las opciones que coinciden (sin importar mayúsculas ni tildes).
 *
 * <pre>
 * ComboBuscable.activar(cmbAlimento, Alimento::getNombre);
 * ComboBuscable.cargar(cmbAlimento, alimentoDAO.listarTodos());
 * </pre>
 */
public final class ComboBuscable {

    private static final String CLAVE_DATOS = "comboBuscable.datos";

    private ComboBuscable() {
    }

    /** Datos internos guardados en el propio ComboBox. */
    private static final class Datos<T> {
        final ObservableList<T> todos = FXCollections.observableArrayList();
        final FilteredList<T> filtrados = new FilteredList<>(todos, t -> true);
        boolean actualizando;
    }

    /**
     * Activa la búsqueda en el ComboBox.
     *
     * @param texto cómo se muestra cada opción (también es el texto en el que se busca)
     */
    public static <T> void activar(ComboBox<T> combo, Function<T, String> texto) {
        Datos<T> datos = new Datos<>();
        combo.getProperties().put(CLAVE_DATOS, datos);
        datos.todos.setAll(combo.getItems());
        combo.setItems(datos.filtrados);
        combo.setEditable(true);
        combo.setVisibleRowCount(10);

        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(T item) {
                return item == null ? "" : texto.apply(item);
            }

            @Override
            public T fromString(String escrito) {
                if (escrito == null || escrito.isBlank()) {
                    return null;
                }
                String buscado = normalizar(escrito);
                return datos.todos.stream()
                        .filter(t -> normalizar(texto.apply(t)).equals(buscado))
                        .findFirst()
                        .orElse(null);
            }
        });

        // Al escribir se filtra la lista y se abre para ver las coincidencias
        combo.getEditor().textProperty().addListener((obs, antes, ahora) -> {
            if (datos.actualizando) {
                return;
            }
            T elegido = combo.getValue();
            if (elegido != null && texto.apply(elegido).equals(ahora)) {
                return; // el texto es el de la opción elegida: no hace falta filtrar
            }
            String buscado = normalizar(ahora);
            Platform.runLater(() -> {
                datos.actualizando = true;
                try {
                    datos.filtrados.setPredicate(t -> coincide(texto.apply(t), buscado));
                    if (combo.getEditor().isFocused() && !datos.filtrados.isEmpty() && !combo.isShowing()) {
                        combo.show();
                    }
                } finally {
                    datos.actualizando = false;
                }
            });
        });

        // Al elegir una opción se muestra la lista completa la próxima vez
        combo.valueProperty().addListener((obs, antes, ahora) -> {
            if (ahora != null) {
                Platform.runLater(() -> datos.filtrados.setPredicate(t -> true));
            }
        });

        // Si el usuario escribe un nombre exacto y sale del campo, se toma esa opción
        combo.getEditor().focusedProperty().addListener((obs, antes, enfocado) -> {
            if (!enfocado) {
                T coincide = combo.getConverter().fromString(combo.getEditor().getText());
                if (coincide != null && coincide != combo.getValue()) {
                    combo.setValue(coincide);
                }
            }
        });
    }

    /** Reemplaza las opciones del ComboBox (usar en lugar de setItems una vez activado). */
    @SuppressWarnings("unchecked")
    public static <T> void cargar(ComboBox<T> combo, Collection<T> opciones) {
        Object guardado = combo.getProperties().get(CLAVE_DATOS);
        if (guardado instanceof Datos<?> datos) {
            Datos<T> d = (Datos<T>) datos;
            d.actualizando = true;
            try {
                d.filtrados.setPredicate(t -> true);
                d.todos.setAll(opciones);
            } finally {
                d.actualizando = false;
            }
        } else {
            combo.setItems(FXCollections.observableArrayList(opciones));
        }
    }

    /**
     * Cada palabra escrita debe ser el inicio de alguna palabra de la opción:
     * "pollo" encuentra "Pechuga de Pollo" pero no "Repollo"; "pech pol" también sirve.
     */
    static boolean coincide(String opcion, String buscadoNormalizado) {
        if (buscadoNormalizado.isEmpty()) {
            return true;
        }
        String[] palabras = normalizar(opcion).split("[^a-z0-9]+");
        for (String parte : buscadoNormalizado.split("\\s+")) {
            boolean encontrada = false;
            for (String palabra : palabras) {
                if (palabra.startsWith(parte)) {
                    encontrada = true;
                    break;
                }
            }
            if (!encontrada) {
                return false;
            }
        }
        return true;
    }

    /** Minúsculas y sin tildes: "Plátano" y "platano" se consideran iguales. */
    static String normalizar(String texto) {
        return Normalizer.normalize(texto == null ? "" : texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
