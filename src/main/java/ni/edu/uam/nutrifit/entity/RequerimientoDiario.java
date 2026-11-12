package ni.edu.uam.nutrifit.entity;

import jakarta.persistence.*;
import ni.edu.uam.proyectofinal.model.Usuario;

import java.time.LocalDate;

@Entity
@Table(name = "requerimientos_diarios")
public class RequerimientoDiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double tmb;

    private Double factorActividad;

    private Double caloriasDiarias;

    private Double proteinas;

    private Double carbohidratos;

    private Double grasas;

    private LocalDate fecha;

    @ManyToOne
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
}
