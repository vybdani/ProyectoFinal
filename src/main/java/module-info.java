module ni.edu.uam.proyectofinal {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires java.sql;

    opens ni.edu.uam.proyectofinal.model to org.hibernate.orm.core, javafx.base;
    opens ni.edu.uam.proyectofinal.controller to javafx.fxml;
    opens ni.edu.uam.proyectofinal to javafx.fxml;
    exports ni.edu.uam.proyectofinal;
}