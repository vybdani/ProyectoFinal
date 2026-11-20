package ni.edu.uam.proyectofinal;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import ni.edu.uam.proyectofinal.service.DatosInicialesService;
import ni.edu.uam.proyectofinal.util.Alertas;
import ni.edu.uam.proyectofinal.util.HibernateUtil;
import ni.edu.uam.proyectofinal.util.Navegacion;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        cargarFuentes();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("views/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 960, 620);
        Navegacion.aplicarEstilos(scene);
        stage.setTitle("Solea - Inicio de sesión");
        stage.setScene(scene);
        // Se puede agrandar y maximizar; el mínimo evita que el diseño se corte
        stage.setResizable(true);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
        verificarBaseDeDatos();
    }

    /**
     * Registra las fuentes del tema (app.css las usa por nombre). Van como TTF porque
     * JavaFX no lee WOFF2. Si alguna falta, JavaFX usa la fuente del sistema.
     */
    private void cargarFuentes() {
        String[] archivos = {
                "Figtree-Regular.ttf", "Figtree-Medium.ttf", "Figtree-SemiBold.ttf", "Figtree-Bold.ttf",
                "InstrumentSerif-Regular.ttf", "InstrumentSerif-Italic.ttf"
        };
        for (String archivo : archivos) {
            try (var fuente = HelloApplication.class.getResourceAsStream("fonts/" + archivo)) {
                if (fuente != null) {
                    Font.loadFont(fuente, 14);
                }
            } catch (IOException e) {
                System.err.println("No se pudo cargar la fuente " + archivo + ": " + e.getMessage());
            }
        }
    }

    /**
     * Conecta con PostgreSQL en segundo plano mientras se ve el login (crear la SessionFactory
     * tarda unos segundos). Si falla, avisa el motivo; la conexión se reintenta al iniciar sesión.
     */
    private void verificarBaseDeDatos() {
        Thread hilo = new Thread(() -> {
            String problema = HibernateUtil.verificarConexion();
            if (problema != null) {
                Platform.runLater(() -> Alertas.error("Sin conexión con la base de datos", problema));
                return;
            }
            try {
                // Alimentos, recetas y ejercicios que trae Solea (solo agrega los que falten)
                new DatosInicialesService().asegurar();
            } catch (Exception e) {
                System.err.println("No se pudieron cargar los datos iniciales: " + e.getMessage());
            }
        }, "conexion-bd");
        hilo.setDaemon(true);
        hilo.start();
    }

    @Override
    public void stop() throws Exception {
        // Cierra la SessionFactory de Hibernate de forma segura al salir de la app
        HibernateUtil.shutdown();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
