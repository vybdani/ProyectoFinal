package ni.edu.uam.proyectofinal.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.util.Navegacion;
import ni.edu.uam.proyectofinal.util.SesionActual;

public class MenuPrincipalController {

    @FXML
    private Label lblBienvenida;

    @FXML
    public void initialize() {
        Usuario usuario = SesionActual.getUsuarioActivo();
        lblBienvenida.setText(usuario != null
                ? "Bienvenido/a, " + usuario.getNombre()
                : "Bienvenido/a a NutriFit");
    }

    @FXML
    private void handleModoNutricion() {
        SesionActual.setModoActual(SesionActual.MODO_NUTRICION);
        Navegacion.volver(lblBienvenida);
    }

    @FXML
    private void handleModoGimnasio() {
        SesionActual.setModoActual(SesionActual.MODO_GIMNASIO);
        Navegacion.volver(lblBienvenida);
    }

    @FXML
    private void handleEditarPerfil() {
        Navegacion.irAlPerfil(lblBienvenida);
    }

    @FXML
    private void handleCerrarSesion() {
        SesionActual.cerrarSesion();
        Navegacion.irAlLogin(lblBienvenida);
    }
}
