package ni.edu.uam.proyectofinal.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "requerimientos_diarios")
public class RequerimientoDiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tmb", nullable = false)
    private Double tmb;

    @Column(name = "factor_actividad", nullable = false)
    private Double factorActividad;

    @Column(name = "calorias_diarias", nullable = false)
    private Double caloriasDiarias;

    @Column(name = "proteinas", nullable = false)
    private Double proteinas;

    @Column(name = "carbohidratos", nullable = false)
    private Double carbohidratos;

    @Column(name = "grasas", nullable = false)
    private Double grasas;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public RequerimientoDiario() {
    }

    public RequerimientoDiario(Double tmb,
                               Double factorActividad,
                               Double caloriasDiarias,
                               Double proteinas,
                               Double carbohidratos,
                               Double grasas,
                               LocalDate fecha,
                               Usuario usuario) {

        this.tmb = tmb;
        this.factorActividad = factorActividad;
        this.caloriasDiarias = caloriasDiarias;
        this.proteinas = proteinas;
        this.carbohidratos = carbohidratos;
        this.grasas = grasas;
        this.fecha = fecha;
        this.usuario = usuario;
    }

    public Long getId() {
        return id;
    }

    public Double getTmb() {
        return tmb;
    }

    public Double getFactorActividad() {
        return factorActividad;
    }

    public Double getCaloriasDiarias() {
        return caloriasDiarias;
    }

    public Double getProteinas() {
        return proteinas;
    }

    public Double getCarbohidratos() {
        return carbohidratos;
    }

    public Double getGrasas() {
        return grasas;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTmb(Double tmb) {
        this.tmb = tmb;
    }

    public void setFactorActividad(Double factorActividad) {
        this.factorActividad = factorActividad;
    }

    public void setCaloriasDiarias(Double caloriasDiarias) {
        this.caloriasDiarias = caloriasDiarias;
    }

    public void setProteinas(Double proteinas) {
        this.proteinas = proteinas;
    }

    public void setCarbohidratos(Double carbohidratos) {
        this.carbohidratos = carbohidratos;
    }

    public void setGrasas(Double grasas) {
        this.grasas = grasas;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
