package ni.edu.uam.proyectofinal.util;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ni.edu.uam.proyectofinal.HelloApplication;
import ni.edu.uam.proyectofinal.controller.ShellController;

import java.io.IOException;

/**
 * Centraliza el cambio de pantallas para no repetir el mismo código en cada controlador.
 *
 * Después de iniciar sesión todo vive en una sola ventana (shell-view.fxml): un menú lateral
 * fijo y un área central. Cuando esa ventana está abierta, {@link #ir} solo cambia el contenido
 * central; antes de iniciar sesión (login y primer perfil) cada pantalla ocupa su propia escena.
 */
public final class Navegacion {

    private static final String VISTA_LOGIN = "login-view.fxml";
    private static final String VISTA_INICIO = "menu-principal-view.fxml";
    private static final String VISTA_PERFIL = "perfil-fisico-view.fxml";

    /** Ancho máximo del contenido de una pantalla; el resto de la ventana queda como margen. */
    public static final double ANCHO_MAXIMO = 1200;

    /** Ventana principal abierta; null mientras no hay sesión iniciada. */
    private static ShellController shell;

    private Navegacion() {
    }

    public static void ir(Node origen, String vista, String titulo, double ancho, double alto) {
        if (shell != null && !VISTA_LOGIN.equals(vista)) {
            shell.mostrar(vista, titulo);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("views/" + vista));
            Stage stage = (Stage) origen.getScene().getWindow();
            Parent pantalla = loader.load();
            // El login ocupa toda la ventana; las demás pantallas se centran con un ancho máximo
            Parent raiz = VISTA_LOGIN.equals(vista) ? pantalla : centrar(pantalla);
            mostrarEnVentana(stage, raiz, ancho, alto, Math.min(ancho, 900), Math.min(alto, 600));
            stage.setTitle("Solea - " + titulo);
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            Alertas.error("No se pudo abrir la pantalla",
                    "Ocurrió un problema al abrir " + titulo + ". Si el problema sigue, revisa la conexión con PostgreSQL.");
        }
    }

    /**
     * Pone una pantalla nueva en la ventana sin crear otra escena: así la ventana conserva
     * su estado (si estaba maximizada, sigue maximizada). Si no lo estaba, toma el tamaño
     * propio de la pantalla nueva y se centra.
     */
    private static void mostrarEnVentana(Stage stage, Parent raiz, double ancho, double alto,
                                         double minimoAncho, double minimoAlto) {
        boolean maximizada = stage.isMaximized();
        Scene escena = stage.getScene();
        if (escena == null) {
            escena = new Scene(raiz, ancho, alto);
            aplicarEstilos(escena);
            stage.setScene(escena);
        } else {
            escena.setRoot(raiz);
        }
        stage.setResizable(true);
        stage.setMinWidth(minimoAncho);
        stage.setMinHeight(minimoAlto);
        if (maximizada) {
            stage.setMaximized(true);
        } else {
            stage.setWidth(Math.max(ancho, minimoAncho));
            stage.setHeight(Math.max(alto, minimoAlto));
            stage.centerOnScreen();
        }
    }

    /**
     * Pone una pantalla dentro de un fondo color papel, arriba y al centro, con un ancho máximo.
     * Así, al maximizar la ventana el contenido no se estira de lado a lado ni deja zonas en blanco.
     */
    public static StackPane centrar(Parent pantalla) {
        if (pantalla instanceof Region region) {
            region.setMaxWidth(ANCHO_MAXIMO);
            region.setMaxHeight(Double.MAX_VALUE);
        }
        StackPane fondo = new StackPane(pantalla);
        fondo.getStyleClass().add("contenido");
        StackPane.setAlignment(pantalla, Pos.TOP_CENTER);
        return fondo;
    }

    /** Agrega la hoja de estilos común (app.css) a una escena. */
    public static void aplicarEstilos(Scene escena) {
        // Color papel detrás de todo: nunca se ve blanco al agrandar la ventana
        escena.setFill(Color.web("#F7F5EE"));
        var hoja = HelloApplication.class.getResource("views/app.css");
        if (hoja != null) {
            escena.getStylesheets().add(hoja.toExternalForm());
        }
    }

    public static void irAlLogin(Node origen) {
        shell = null;
        ir(origen, VISTA_LOGIN, "Inicio de sesión", 960, 620);
    }

    /** Abre la ventana principal (si hace falta) y muestra el inicio. */
    public static void irAlMenuPrincipal(Node origen) {
        if (shell == null && !abrirVentanaPrincipal(origen)) {
            return;
        }
        shell.mostrar(VISTA_INICIO, "Inicio");
    }

    public static void irAlPerfil(Node origen) {
        ir(origen, VISTA_PERFIL, "Perfil físico", 760, 680);
    }

    /** Con el menú lateral siempre visible, "volver" lleva al inicio. */
    public static void volver(Node origen) {
        irAlMenuPrincipal(origen);
    }

    private static boolean abrirVentanaPrincipal(Node origen) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("views/shell-view.fxml"));
            Parent raiz = loader.load();
            shell = loader.getController();
            Stage stage = (Stage) origen.getScene().getWindow();
            mostrarEnVentana(stage, raiz, 1240, 800, 1100, 720);
            return true;
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            shell = null;
            Alertas.error("No se pudo abrir Solea",
                    "Ocurrió un problema al abrir la ventana principal. Si el problema sigue, revisa la conexión con PostgreSQL.");
            return false;
        }
    }
}
