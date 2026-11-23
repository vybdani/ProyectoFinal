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
 */
public final class ComboBuscable {

    private static final String CLAVE_DATOS = "comboBuscable.datos";

    private ComboBuscable() {
    }

    /** Datos internos guardados en el propio ComboBox. */
    private static final class Datos<T> {
        final ObservableList<T> todos = FXCollections.observableArrayList();
        final FilteredList<T> filtrados = new FilteredList<>(todos, t -> true);
        Function<T, String> texto;
        boolean actualizando;
    }

    /**
     * Activa la búsqueda en el ComboBox.
     *
     * @param texto cómo se muestra cada opción (también es el texto en el que se busca)
     */
    public static <T> void activar(ComboBox<T> combo, Function<T, String> texto) {
        Datos<T> datos = new Datos<>();
        datos.texto = texto;
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

                // 1. Coincidencia exacta con el texto completo mostrado
                for (T t : datos.todos) {
                    if (normalizar(texto.apply(t)).equals(buscado)) {
                        return t;
                    }
                }
                // 2. Coincidencia exacta con el nombre base (antes de " · ")
                for (T t : datos.todos) {
                    String etiqueta = texto.apply(t);
                    int sep = etiqueta.indexOf("·");
                    String base = sep > 0 ? etiqueta.substring(0, sep) : etiqueta;
                    if (normalizar(base).equals(buscado)) {
                        return t;
                    }
                }
                // 3. Si al filtrar queda una única coincidencia clara, se toma esa
                if (datos.filtrados.size() == 1 && coincide(texto.apply(datos.filtrados.get(0)), buscado)) {
                    return datos.filtrados.get(0);
                }
                return null;
            }
        });

        // Al escribir se filtra la lista sin perder la selección si coincide
        combo.getEditor().textProperty().addListener((obs, antes, ahora) -> {
            if (datos.actualizando) {
                return;
            }
            T elegido = combo.getValue();
            if (elegido != null && texto.apply(elegido).equals(ahora)) {
                return;
            }
            String buscado = normalizar(ahora);
            Platform.runLater(() -> {
                if (datos.actualizando) {
                    return;
                }
                datos.actualizando = true;
                try {
                    T actual = combo.getValue();
                    if (actual != null && !coincide(texto.apply(actual), buscado)) {
                        combo.setValue(null);
                    }
                    datos.filtrados.setPredicate(t -> coincide(texto.apply(t), buscado));
                    if (combo.getEditor().isFocused() && !datos.filtrados.isEmpty() && !combo.isShowing()) {
                        combo.show();
                    }
                } finally {
                    datos.actualizando = false;
                }
            });
        });

        // Al abrir el desplegable después de haber elegido algo, vuelve a mostrar todas las opciones
        combo.showingProperty().addListener((obs, antes, mostrando) -> {
            if (!mostrando || datos.actualizando) {
                return;
            }
            T elegido = combo.getValue();
            String escrito = combo.getEditor().getText();
            if (elegido == null && (escrito == null || escrito.isBlank())) {
                datos.actualizando = true;
                try {
                    datos.filtrados.setPredicate(t -> true);
                } finally {
                    datos.actualizando = false;
                }
            } else if (elegido != null && texto.apply(elegido).equals(escrito)) {
                datos.actualizando = true;
                try {
                    datos.filtrados.setPredicate(t -> true);
                    combo.setValue(elegido);
                    combo.getSelectionModel().select(elegido);
                    combo.getEditor().setText(texto.apply(elegido));
                } finally {
                    datos.actualizando = false;
                }
            }
        });

        // Si el usuario escribe un nombre válido y sale del campo, confirma la selección
        combo.getEditor().focusedProperty().addListener((obs, antes, enfocado) -> {
            if (!enfocado && !datos.actualizando) {
                T coincide = combo.getConverter().fromString(combo.getEditor().getText());
                if (coincide != null && coincide != combo.getValue()) {
                    seleccionar(combo, coincide);
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

    /** Selecciona programáticamente un elemento (o limpia con null) sin desincronizar el editor. */
    @SuppressWarnings("unchecked")
    public static <T> void seleccionar(ComboBox<T> combo, T valor) {
        Object guardado = combo.getProperties().get(CLAVE_DATOS);
        if (guardado instanceof Datos<?> datos) {
            Datos<T> d = (Datos<T>) datos;
            d.actualizando = true;
            try {
                d.filtrados.setPredicate(t -> true);
                combo.setValue(valor);
                if (valor != null) {
                    combo.getSelectionModel().select(valor);
                    combo.getEditor().setText(d.texto.apply(valor));
                } else {
                    combo.getSelectionModel().clearSelection();
                    combo.getEditor().clear();
                }
            } finally {
                d.actualizando = false;
            }
        } else {
            combo.setValue(valor);
        }
    }

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

    static String normalizar(String texto) {
        return Normalizer.normalize(texto == null ? "" : texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}