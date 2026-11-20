package ni.edu.uam.proyectofinal;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ni.edu.uam.proyectofinal.util.Alertas;
import ni.edu.uam.proyectofinal.util.HibernateUtil;
import ni.edu.uam.proyectofinal.util.Navegacion;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("views/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 480);
        Navegacion.aplicarEstilos(scene);
        stage.setTitle("Solea - Inicio de Sesión");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        verificarBaseDeDatos();
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
