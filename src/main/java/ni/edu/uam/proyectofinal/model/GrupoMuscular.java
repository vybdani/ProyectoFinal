package ni.edu.uam.proyectofinal.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "grupos_musculares")
public class GrupoMuscular {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(length = 200)
    private String descripcion;

    /**
     * Sin cascade: los ejercicios son un catálogo propio y no se borran al tocar el grupo.
     */
    @OneToMany(mappedBy = "grupoMuscular")
    private List<Ejercicio> ejercicios = new ArrayList<>();

    public GrupoMuscular() {
    }

    public GrupoMuscular(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<Ejercicio> getEjercicios() {
        return ejercicios;
    }

    public void setEjercicios(List<Ejercicio> ejercicios) {
        this.ejercicios = ejercicios;
    }

    /** Agrega un ejercicio al grupo manteniendo la relación en ambos lados. */
    public void agregarEjercicio(Ejercicio ejercicio) {
        ejercicios.add(ejercicio);
        ejercicio.setGrupoMuscular(this);
    }

    public void removerEjercicio(Ejercicio ejercicio) {
        ejercicios.remove(ejercicio);
        ejercicio.setGrupoMuscular(null);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
