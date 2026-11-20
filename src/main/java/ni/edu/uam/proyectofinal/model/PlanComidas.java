package ni.edu.uam.proyectofinal.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "planes_comidas")
public class PlanComidas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requerimiento_id")
    private RequerimientoDiario requerimientoDiario;

    @Column(nullable = false, length = 120)
    private String nombrePlan;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "calorias_objetivo_diarias")
    private Double caloriasObjetivoDiarias;

    /**
     * Composición: las comidas no existen sin su plan (cascade ALL + orphanRemoval).
     * LAZY: PlanComidasDAO las trae con JOIN FETCH.
     */
    @OneToMany(mappedBy = "planComidas", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComidaPlan> comidas = new ArrayList<>();

    public PlanComidas() {
    }

    public PlanComidas(Usuario usuario, RequerimientoDiario requerimientoDiario, String nombrePlan, LocalDate fechaInicio, Double caloriasObjetivoDiarias) {
        this.usuario = usuario;
        this.requerimientoDiario = requerimientoDiario;
        this.nombrePlan = nombrePlan;
        this.fechaInicio = fechaInicio;
        this.caloriasObjetivoDiarias = caloriasObjetivoDiarias;
    }
    public void agregarComida(ComidaPlan comida) {
        comidas.add(comida);
        comida.setPlanComidas(this);
    }

    public void removerComida(ComidaPlan comida) {
        comidas.remove(comida);
        comida.setPlanComidas(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public RequerimientoDiario getRequerimientoDiario() {
        return requerimientoDiario;
    }

    public void setRequerimientoDiario(RequerimientoDiario requerimientoDiario) {
        this.requerimientoDiario = requerimientoDiario;
    }

    public String getNombrePlan() {
        return nombrePlan;
    }

    public void setNombrePlan(String nombrePlan) {
        this.nombrePlan = nombrePlan;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Double getCaloriasObjetivoDiarias() {
        return caloriasObjetivoDiarias;
    }

    public void setCaloriasObjetivoDiarias(Double caloriasObjetivoDiarias) {
        this.caloriasObjetivoDiarias = caloriasObjetivoDiarias;
    }

    public List<ComidaPlan> getComidas() {
        return comidas;
    }

    public void setComidas(List<ComidaPlan> comidas) {
        this.comidas = comidas;
    }
}