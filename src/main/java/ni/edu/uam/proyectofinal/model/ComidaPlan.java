package ni.edu.uam.proyectofinal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "comidas_plan")
public class ComidaPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private PlanComidas planComidas;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receta_id", nullable = false)
    private Receta receta;

    @Column(nullable = false, length = 20)
    private String diaSemana;

    @Column(nullable = false, length = 30)
    private String tiempoComida;

    private Double caloriasEstimadas;

    public ComidaPlan() {
    }

    public ComidaPlan(Receta receta, String diaSemana, String tiempoComida) {
        this.receta = receta;
        this.diaSemana = diaSemana;
        this.tiempoComida = tiempoComida;
        this.caloriasEstimadas = receta != null ? receta.getCaloriasTotales() : 0.0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PlanComidas getPlanComidas() {
        return planComidas;
    }

    public void setPlanComidas(PlanComidas planComidas) {
        this.planComidas = planComidas;
    }

    public Receta getReceta() {
        return receta;
    }

    public void setReceta(Receta receta) {
        this.receta = receta;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public String getTiempoComida() {
        return tiempoComida;
    }

    public void setTiempoComida(String tiempoComida) {
        this.tiempoComida = tiempoComida;
    }

    public Double getCaloriasEstimadas() {
        return caloriasEstimadas;
    }

    public void setCaloriasEstimadas(Double caloriasEstimadas) {
        this.caloriasEstimadas = caloriasEstimadas;
    }
}