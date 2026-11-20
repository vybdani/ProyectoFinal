package ni.edu.uam.proyectofinal.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import ni.edu.uam.proyectofinal.dao.UsuarioDAO;
import ni.edu.uam.proyectofinal.model.PerfilFisico;
import ni.edu.uam.proyectofinal.model.Usuario;
import ni.edu.uam.proyectofinal.util.Alertas;
import ni.edu.uam.proyectofinal.util.Navegacion;
import ni.edu.uam.proyectofinal.util.SesionActual;
import ni.edu.uam.proyectofinal.util.Validacion;
import ni.edu.uam.proyectofinal.util.Validacion.ValidacionException;

public class PerfilFisicoController {

    /** Libras que tiene un kilogramo. */
    private static final double LIBRAS_POR_KG = 2.20462;
    private static final String KG = "kg";
    private static final String LB = "lb";

    @FXML private TextField txtPeso;
    @FXML private ComboBox<String> cmbUnidadPeso;
    @FXML private TextField txtAltura;
    @FXML private TextField txtEdad;
    @FXML private TextField txtPorcentajeGrasa;
    @FXML private ComboBox<String> cmbSexo;
    @FXML private ComboBox<String> cmbNivelActividad;
    @FXML private Button btnVolver;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private Usuario usuarioActual;

    @FXML
    public void initialize() {
        cmbSexo.setItems(FXCollections.observableArrayList("Masculino", "Femenino"));
        // Los valores deben coincidir con los que entiende CalculoNutricionalService
        cmbNivelActividad.setItems(FXCollections.observableArrayList("Sedentario", "Ligero", "Moderado", "Alto"));

        // El peso se puede escribir en kilos o en libras; en la base siempre se guarda en kg
        cmbUnidadPeso.setItems(FXCollections.observableArrayList(KG, LB));
        cmbUnidadPeso.setValue(KG);
        cmbUnidadPeso.valueProperty().addListener((obs, antes, ahora) -> convertirPesoEscrito(antes, ahora));

        usuarioActual = SesionActual.getUsuarioActivo();
        PerfilFisico p = usuarioActual != null ? usuarioActual.getPerfilFisico() : null;

        // Si todavía no tiene perfil (recién registrado) no puede volver al menú sin guardarlo
        btnVolver.setVisible(p != null);
        btnVolver.setManaged(p != null);

        if (p != null) {
            if (p.getPeso() != null) txtPeso.setText(String.valueOf(p.getPeso()));
            if (p.getAltura() != null) txtAltura.setText(String.valueOf(p.getAltura()));
            if (p.getEdad() != null) txtEdad.setText(String.valueOf(p.getEdad()));
            if (p.getPorcentajeGrasa() != null && p.getPorcentajeGrasa() > 0) {
                txtPorcentajeGrasa.setText(String.valueOf(p.getPorcentajeGrasa()));
            }
            cmbSexo.setValue(p.getSexo());
            cmbNivelActividad.setValue(p.getNivelActividad());
        }
    }

    /** Si ya había un peso escrito, lo convierte a la unidad nueva (70 kg -> 154.3 lb). */
    private void convertirPesoEscrito(String unidadAnterior, String unidadNueva) {
        if (unidadAnterior == null || unidadNueva == null || unidadAnterior.equals(unidadNueva)) {
            return;
        }
        String texto = txtPeso.getText() == null ? "" : txtPeso.getText().trim().replace(',', '.');
        try {
            double valor = Double.parseDouble(texto);
            if (Double.isFinite(valor) && valor > 0) {
                double convertido = LB.equals(unidadNueva) ? valor * LIBRAS_POR_KG : valor / LIBRAS_POR_KG;
                txtPeso.setText(String.valueOf(redondear(convertido)));
            }
        } catch (NumberFormatException e) {
            // Campo vacío o con texto: se deja como está y la validación avisará al guardar
        }
    }

    private static double redondear(double valor) {
        return Math.round(valor * 10) / 10.0;
    }

    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    @FXML
    private void handleGuardarPerfil() {
        if (usuarioActual == null) {
            usuarioActual = SesionActual.getUsuarioActivo();
        }
        if (usuarioActual == null) {
            Alertas.aviso("Sesión requerida", "Inicia sesión para guardar tu perfil.");
            Navegacion.irAlLogin(txtPeso);
            return;
        }

        try {
            double peso = LB.equals(cmbUnidadPeso.getValue())
                    ? redondear(Validacion.decimal(txtPeso, "el peso (lb)", 66, 661) / LIBRAS_POR_KG)
                    : Validacion.decimal(txtPeso, "el peso (kg)", 30, 300);
            double altura = Validacion.decimal(txtAltura, "la altura (cm)", 120, 230);
            int edad = Validacion.entero(txtEdad, "la edad", 18, 100);
            Double grasa = Validacion.decimalOpcional(txtPorcentajeGrasa, "el % de grasa", 3, 60);
            String sexo = Validacion.seleccion(cmbSexo, "tu sexo");
            String nivel = Validacion.seleccion(cmbNivelActividad, "tu nivel de actividad");

            // IMC fuera de rango humano: probablemente se confundieron peso y altura
            double imc = peso / Math.pow(altura / 100.0, 2);
            if (imc < 12 || imc > 70) {
                throw Validacion.fallo(txtPeso, String.format(
                        "Con %.1f kg y %.0f cm el IMC sería %.1f, que no es realista. Revisa el peso y la altura.",
                        peso, altura, imc));
            }

            PerfilFisico perfil = usuarioActual.getPerfilFisico();
            if (perfil == null) {
                perfil = new PerfilFisico(peso, altura, edad, grasa, sexo, nivel);
                usuarioActual.setPerfilFisico(perfil);
            } else {
                perfil.setPeso(peso);
                perfil.setAltura(altura);
                perfil.setEdad(edad);
                perfil.setPorcentajeGrasa(grasa);
                perfil.setSexo(sexo);
                perfil.setNivelActividad(nivel);
            }

            usuarioDAO.actualizar(usuarioActual);
            // Se recarga desde la BD para tener los ids generados y evitar duplicar el perfil
            Usuario recargado = usuarioDAO.obtenerUsuarioConPerfil(usuarioActual.getId());
            SesionActual.setUsuarioActivo(recargado != null ? recargado : usuarioActual);

            Alertas.info("Perfil guardado", "Tus datos físicos se guardaron. Recalcula tu requerimiento para actualizar tus metas.");
            Navegacion.irAlMenuPrincipal(txtPeso);
        } catch (ValidacionException e) {
            Alertas.aviso("Revisa tus datos", e.getMessage());
        } catch (Exception e) {
            Alertas.error("No se pudo guardar el perfil", e);
        }
    }

    @FXML
    private void handleVolver() {
        Navegacion.irAlMenuPrincipal(txtPeso);
    }
}
