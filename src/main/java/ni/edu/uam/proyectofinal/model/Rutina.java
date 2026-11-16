package ni.edu.uam.proyectofinal.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rutinas")
public class Rutina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    /** Objetivo de gimnasio con el que se generó: Volumen muscular o Definición. */
    @Column(nullable = false, length = 40)
    private String objetivo;

    @Column(nullable = false)
    private Integer diasPorSemana;

    private LocalDate fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "rutina", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dia ASC, orden ASC")
    private List<RutinaEjercicio> ejercicios = new ArrayList<>();

    public Rutina() {
    }

    public Rutina(String nombre, String objetivo, Integer diasPorSemana, Usuario usuario) {
        this.nombre = nombre;
        this.objetivo = objetivo;
        this.diasPorSemana = diasPorSemana;
        this.usuario = usuario;
        this.fechaCreacion = LocalDate.now();
    }

    /** Agrega un ejercicio a la rutina manteniendo la relación en ambos lados. */
    public void agregarEjercicio(RutinaEjercicio rutinaEjercicio) {
        rutinaEjercicio.setRutina(this);
        ejercicios.add(rutinaEjercicio);
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

    public String getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    public Integer getDiasPorSemana() {
        return diasPorSemana;
    }

    public void setDiasPorSemana(Integer diasPorSemana) {
        this.diasPorSemana = diasPorSemana;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<RutinaEjercicio> getEjercicios() {
        return ejercicios;
    }

    public void setEjercicios(List<RutinaEjercicio> ejercicios) {
        this.ejercicios = ejercicios;
    }
}
