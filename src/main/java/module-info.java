module ni.edu.uam.proyectofinal {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires java.naming;
    requires java.sql;
    requires java.desktop;
    requires java.net.http;
    requires com.google.gson;
    requires jasperreports;
    // Librerías que JasperReports usa en tiempo de ejecución. Al ser módulos automáticos,
    // Java solo los carga si alguien los pide: sin esto el PDF falla con NoClassDefFoundError.
    requires com.github.librepdf.openpdf;   // exportación a PDF
    requires ecj;                           // compila las plantillas .jrxml
    requires commons.beanutils;             // lee los getters de las filas del reporte
    requires commons.digester;              // lee el XML de las plantillas
    requires org.apache.commons.collections4;
    requires commons.logging;

    opens ni.edu.uam.proyectofinal to javafx.fxml;
    opens ni.edu.uam.proyectofinal.controller to javafx.fxml;
    opens ni.edu.uam.proyectofinal.model to org.hibernate.orm.core, javafx.base, com.google.gson, jasperreports;

    exports ni.edu.uam.proyectofinal;
    exports ni.edu.uam.proyectofinal.model;
    // JasperReports (commons-beanutils) lee las filas de los reportes con sus getters públicos
    exports ni.edu.uam.proyectofinal.reporte;
}
