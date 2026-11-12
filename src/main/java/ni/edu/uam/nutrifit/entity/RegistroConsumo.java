package ni.edu.uam.nutrifit.entity;

import jakarta.persistence.*;
import ni.edu.uam.proyectofinal.model.Usuario;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "registros_consumo")
public class RegistroConsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "alimento_id", nullable = false)
    private Alimento alimento;

    private Double cantidad;

    private Double caloriasConsumidas;

    private Double proteinasConsumidas;

    private Double carbohidratosConsumidos;

    private Double grasasConsumidas;

    private String tipoComida;

    private LocalDate fecha;

    private LocalTime hora;

    public RegistroConsumo() {
    }

    public RegistroConsumo(Usuario usuario,
                           Alimento alimento,
                           Double cantidad,
                           String tipoComida) {

        this.usuario = usuario;
        this.alimento = alimento;
        this.cantidad = cantidad;
        this.tipoComida = tipoComida;

        this.fecha = LocalDate.now();
        this.hora = LocalTime.now();

        double factor = cantidad / alimento.getCantidadBase();

        this.caloriasConsumidas =
                alimento.getCalorias() * factor;

        this.proteinasConsumidas =
                alimento.getProteinas() * factor;

        this.carbohidratosConsumidos =
                alimento.getCarbohidratos() * factor;

        this.grasasConsumidas =
                alimento.getGrasas() * factor;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Alimento getAlimento() {
        return alimento;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public Double getCaloriasConsumidas() {
        return caloriasConsumidas;
    }

    public Double getProteinasConsumidas() {
        return proteinasConsumidas;
    }

    public Double getCarbohidratosConsumidos() {
        return carbohidratosConsumidos;
    }

    public Double getGrasasConsumidas() {
        return grasasConsumidas;
    }

    public String getTipoComida() {
        return tipoComida;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }
}
