module ni.edu.uam.proyectofinal {
    requires javafx.controls;
    requires javafx.fxml;


    opens ni.edu.uam.proyectofinal to javafx.fxml;
    exports ni.edu.uam.proyectofinal;
}