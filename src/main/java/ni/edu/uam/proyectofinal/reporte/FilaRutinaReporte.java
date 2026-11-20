package ni.edu.uam.proyectofinal.reporte;

import ni.edu.uam.proyectofinal.model.RutinaEjercicio;

/**
 * Una fila del reporte ReporteRutinaGimnasio.jrxml.
 * JasperReports lee los campos $F{...} con los getters, por eso es un JavaBean y no un record.
 */
public class FilaRutinaReporte {

    private final Integer diaSemana;
    private final String nombreDia;
    private final String nombreEjercicio;
    private final String grupoMuscular;
    private final Integer series;
    private final String repeticiones;
    private final Integer descansoSegundos;

    public FilaRutinaReporte(RutinaEjercicio re) {
        this.diaSemana = re.getDia();
        this.nombreDia = re.getNombreDia();
        this.nombreEjercicio = re.getEjercicio().getNombre();
        this.grupoMuscular = re.getEjercicio().getGrupoMuscular().getNombre();
        this.series = re.getSeries();
        this.repeticiones = re.getRepeticiones();
        this.descansoSegundos = re.getDescansoSegundos();
    }

    public Integer getDiaSemana() {
        return diaSemana;
    }

    public String getNombreDia() {
        return nombreDia;
    }

    public String getNombreEjercicio() {
        return nombreEjercicio;
    }

    public String getGrupoMuscular() {
        return grupoMuscular;
    }

    public Integer getSeries() {
        return series;
    }

    public String getRepeticiones() {
        return repeticiones;
    }

    public Integer getDescansoSegundos() {
        return descansoSegundos;
    }
}
