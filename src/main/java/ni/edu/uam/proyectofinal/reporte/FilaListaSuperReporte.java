package ni.edu.uam.proyectofinal.reporte;

import ni.edu.uam.proyectofinal.model.ItemListaSuper;

/** Una fila del reporte ReporteListaSuper.jrxml (JavaBean leído por JasperReports). */
public class FilaListaSuperReporte {

    private final String alimento;
    private final Double cantidad;
    private final String unidad;
    private final Boolean comprado;

    public FilaListaSuperReporte(ItemListaSuper item) {
        this.alimento = item.getNombreProducto();
        this.cantidad = item.getCantidad();
        this.unidad = item.getUnidad();
        this.comprado = Boolean.TRUE.equals(item.getComprado());
    }

    public String getAlimento() {
        return alimento;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public String getUnidad() {
        return unidad;
    }

    public Boolean getComprado() {
        return comprado;
    }
}
