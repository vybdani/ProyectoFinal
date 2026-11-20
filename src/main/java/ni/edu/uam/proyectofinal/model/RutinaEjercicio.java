package ni.edu.uam.proyectofinal.model;

import jakarta.persistence.*;

/**
 * Un ejercicio dentro de una rutina: en qué día se hace, en qué orden,
 * cuántas series, repeticiones y descanso.
 */
@Entity
@Table(name = "rutina_ejercicios")
public class RutinaEjercicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rutina_id", nullable = false)
    private Rutina rutina;

    /** EAGER a propósito: la rutina siempre se muestra con el nombre de cada ejercicio. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ejercicio_id", nullable = false)
    private Ejercicio ejercicio;

    /** Número de día dentro de la rutina (1, 2, 3...). */
    @Column(nullable = false)
    private Integer dia;

    /** Nombre del día, por ejemplo "Empuje" o "Pierna". */
    @Column(nullable = false, length = 40)
    private String nombreDia;

    @Column(nullable = false)
    private Integer orden;

    @Column(nullable = false)
    private Integer series;

    @Column(nullable = false, length = 20)
    private String repeticiones;

    @Column(nullable = false)
    private Integer descansoSegundos;

    public RutinaEjercicio() {
    }

    public RutinaEjercicio(Ejercicio ejercicio, Integer dia, String nombreDia, Integer orden,
                           Integer series, String repeticiones, Integer descansoSegundos) {
        this.ejercicio = ejercicio;
        this.dia = dia;
        this.nombreDia = nombreDia;
        this.orden = orden;
        this.series = series;
        this.repeticiones = repeticiones;
        this.descansoSegundos = descansoSegundos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Rutina getRutina() {
        return rutina;
    }

    public void setRutina(Rutina rutina) {
        this.rutina = rutina;
    }

    public Ejercicio getEjercicio() {
        return ejercicio;
    }

    public void setEjercicio(Ejercicio ejercicio) {
        this.ejercicio = ejercicio;
    }

    public Integer getDia() {
        return dia;
    }

    public void setDia(Integer dia) {
        this.dia = dia;
    }

    public String getNombreDia() {
        return nombreDia;
    }

    public void setNombreDia(String nombreDia) {
        this.nombreDia = nombreDia;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public Integer getSeries() {
        return series;
    }

    public void setSeries(Integer series) {
        this.series = series;
    }

    public String getRepeticiones() {
        return repeticiones;
    }

    public void setRepeticiones(String repeticiones) {
        this.repeticiones = repeticiones;
    }

    public Integer getDescansoSegundos() {
        return descansoSegundos;
    }

    public void setDescansoSegundos(Integer descansoSegundos) {
        this.descansoSegundos = descansoSegundos;
    }
}
