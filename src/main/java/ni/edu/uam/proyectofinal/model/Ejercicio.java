package ni.edu.uam.proyectofinal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ejercicios")
public class Ejercicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 60)
    private String equipo;

    @Column(length = 300)
    private String descripcion;

    /** EAGER a propósito: el grupo es un dato pequeño que siempre se muestra junto al ejercicio. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "grupo_muscular_id", nullable = false)
    private GrupoMuscular grupoMuscular;

    public Ejercicio() {
    }

    public Ejercicio(String nombre, String equipo, String descripcion, GrupoMuscular grupoMuscular) {
        this.nombre = nombre;
        this.equipo = equipo;
        this.descripcion = descripcion;
        this.grupoMuscular = grupoMuscular;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEquipo() {
        return equipo;
    }

    public void setEquipo(String equipo) {
        this.equipo = equipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public GrupoMuscular getGrupoMuscular() {
        return grupoMuscular;
    }

    public void setGrupoMuscular(GrupoMuscular grupoMuscular) {
        this.grupoMuscular = grupoMuscular;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
