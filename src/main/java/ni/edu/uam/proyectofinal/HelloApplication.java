package ni.edu.uam.proyectofinal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ni.edu.uam.proyectofinal.util.HibernateUtil;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Carga la vista de Login ubicada en src/main/resources/ni/edu/uam/proyectofinal/views/login-view.fxml
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("views/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 480);
        stage.setTitle("NutriFit App - Inicio de Sesión");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
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
