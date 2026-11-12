package ni.edu.uam.proyectofinal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "receta_ingredientes")
public class RecetaIngrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receta_id", nullable = false)
    private Receta receta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "alimento_id", nullable = false)
    private Alimento alimento;

    @Column(nullable = false)
    private Double cantidadGramos;

    public RecetaIngrediente() {
    }

    public RecetaIngrediente(Alimento alimento, Double cantidadGramos) {
        this.alimento = alimento;
        this.cantidadGramos = cantidadGramos;
    }

    private double getFactor() {
        if (alimento == null || alimento.getCantidadBase() == null || alimento.getCantidadBase() == 0) {
            return 0.0;
        }
        return cantidadGramos / alimento.getCantidadBase();
    }

    public double getCalorias() {
        return (alimento != null && alimento.getCalorias() != null) ? alimento.getCalorias() * getFactor() : 0.0;
    }

    public double getProteinas() {
        return (alimento != null && alimento.getProteinas() != null) ? alimento.getProteinas() * getFactor() : 0.0;
    }

    public double getCarbohidratos() {
        return (alimento != null && alimento.getCarbohidratos() != null) ? alimento.getCarbohidratos() * getFactor() : 0.0;
    }

    public double getGrasas() {
        return (alimento != null && alimento.getGrasas() != null) ? alimento.getGrasas() * getFactor() : 0.0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Receta getReceta() {
        return receta;
    }

    public void setReceta(Receta receta) {
        this.receta = receta;
    }

    public Alimento getAlimento() {
        return alimento;
    }

    public void setAlimento(Alimento alimento) {
        this.alimento = alimento;
    }

    public Double getCantidadGramos() {
        return cantidadGramos;
    }

    public void setCantidadGramos(Double cantidadGramos) {
        this.cantidadGramos = cantidadGramos;
    }
}