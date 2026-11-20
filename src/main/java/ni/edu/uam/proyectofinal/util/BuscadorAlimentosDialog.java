package ni.edu.uam.proyectofinal.util;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import ni.edu.uam.proyectofinal.model.Alimento;
import ni.edu.uam.proyectofinal.model.AlimentoExterno;
import ni.edu.uam.proyectofinal.service.AlimentoService;
import ni.edu.uam.proyectofinal.service.NutricionApiClient;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;

/**
 * Ventana para buscar alimentos en internet (Open Food Facts, a través de la API de Solea)
 * y guardarlos en la base de datos para usarlos en recetas y en el registro de consumo.
 */
public final class BuscadorAlimentosDialog {

    private static final int RESULTADOS = 10;

    private BuscadorAlimentosDialog() {
    }

    /** Muestra la ventana; devuelve el alimento guardado en la BD, o vacío si se canceló. */
    public static Optional<Alimento> mostrar(Node origen) {
        NutricionApiClient api = new NutricionApiClient();
        AlimentoService alimentoService = new AlimentoService();

        Dialog<Alimento> dialogo = new Dialog<>();
        dialogo.setTitle("Solea - Buscar alimento en internet");
        dialogo.setHeaderText("Busca un alimento en Open Food Facts y agrégalo a tus alimentos");
        if (origen != null && origen.getScene() != null) {
            dialogo.initOwner(origen.getScene().getWindow());
        }

        TextField txtBusqueda = new TextField();
        txtBusqueda.setPromptText("Ejemplo: avena, yogur griego, pechuga de pollo");
        HBox.setHgrow(txtBusqueda, Priority.ALWAYS);
        Button btnBuscar = new Button("Buscar");
        btnBuscar.setDefaultButton(true);
        Label lblEstado = new Label("Los valores son por cada 100 g.");
        lblEstado.setWrapText(true);

        ListView<AlimentoExterno> lista = new ListView<>();
        lista.setPrefSize(560, 300);
        lista.setPlaceholder(new Label("Escribe un alimento y pulsa Buscar."));
        lista.setCellFactory(v -> new ListCell<>() {
            @Override
            protected void updateItem(AlimentoExterno a, boolean vacia) {
                super.updateItem(a, vacia);
                setText(vacia || a == null ? null : String.format(
                        "%s%n%s · %.0f kcal · P %.1f g · C %.1f g · G %.1f g",
                        a.getNombre(), a.getMarca() != null ? a.getMarca() : "Genérico",
                        a.getCaloriasPor100g(), a.getProteinasPor100g(),
                        a.getCarbohidratosPor100g(), a.getGrasasPor100g()));
            }
        });

        VBox contenido = new VBox(10, new HBox(8, txtBusqueda, btnBuscar), lista, lblEstado);
        contenido.setPadding(new Insets(10));
        dialogo.getDialogPane().setContent(contenido);

        ButtonType guardar = new ButtonType("Agregar a mis alimentos", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);
        Node btnGuardar = dialogo.getDialogPane().lookupButton(guardar);
        btnGuardar.disableProperty().bind(lista.getSelectionModel().selectedItemProperty().isNull());

        Runnable buscar = () -> {
            String texto = txtBusqueda.getText() == null ? "" : txtBusqueda.getText().trim();
            if (texto.length() < 2 || texto.length() > 60) {
                lblEstado.setText("Escribe entre 2 y 60 caracteres para buscar.");
                txtBusqueda.requestFocus();
                return;
            }
            btnBuscar.setDisable(true);
            lista.getItems().clear();
            lblEstado.setText("Buscando \"" + texto + "\" en internet…");
            api.buscarAlimentosAsync(texto, RESULTADOS).whenComplete((resultados, error) -> Platform.runLater(() -> {
                btnBuscar.setDisable(false);
                if (error != null) {
                    Throwable causa = error instanceof CompletionException && error.getCause() != null
                            ? error.getCause() : error;
                    lblEstado.setText(causa.getMessage());
                    return;
                }
                mostrarResultados(lista, lblEstado, texto, resultados);
            }));
        };
        btnBuscar.setOnAction(e -> buscar.run());
        txtBusqueda.setOnAction(e -> buscar.run());

        // Guardar en la BD al pulsar "Agregar"; si falla, el diálogo sigue abierto
        Alimento[] guardado = new Alimento[1];
        btnGuardar.addEventFilter(javafx.event.ActionEvent.ACTION, evento -> {
            AlimentoExterno elegido = lista.getSelectionModel().getSelectedItem();
            try {
                guardado[0] = alimentoService.importar(elegido);
            } catch (Exception e) {
                evento.consume();
                Alertas.error("No se pudo agregar el alimento", e);
            }
        });
        dialogo.setResultConverter(boton -> boton == guardar ? guardado[0] : null);

        Platform.runLater(txtBusqueda::requestFocus);
        return dialogo.showAndWait();
    }

    private static void mostrarResultados(ListView<AlimentoExterno> lista, Label lblEstado, String texto,
                                          List<AlimentoExterno> resultados) {
        lista.getItems().setAll(resultados);
        if (resultados.isEmpty()) {
            lblEstado.setText("No se encontraron alimentos con información nutricional para \"" + texto + "\".");
            return;
        }
        String fuente = resultados.get(0).getFuente();
        lblEstado.setText(resultados.size() + " resultado(s) · Fuente: " + fuente + " · valores por 100 g.");
        lista.getSelectionModel().selectFirst();
    }
}
