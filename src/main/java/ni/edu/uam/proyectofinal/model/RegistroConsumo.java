package ni.edu.uam.proyectofinal.model;

import jakarta.persistence.*;

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

        this.caloriasConsumidas = alimento.getCalorias() * factor;
        this.proteinasConsumidas = alimento.getProteinas() * factor;
        this.carbohidratosConsumidos = alimento.getCarbohidratos() * factor;
        this.grasasConsumidas = alimento.getGrasas() * factor;
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

    public Alimento getAlimento() {
        return alimento;
    }

    public void setAlimento(Alimento alimento) {
        this.alimento = alimento;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }

    public Double getCaloriasConsumidas() {
        return caloriasConsumidas;
    }

    public void setCaloriasConsumidas(Double caloriasConsumidas) {
        this.caloriasConsumidas = caloriasConsumidas;
    }

    public Double getProteinasConsumidas() {
        return proteinasConsumidas;
    }

    public void setProteinasConsumidas(Double proteinasConsumidas) {
        this.proteinasConsumidas = proteinasConsumidas;
    }

    public Double getCarbohidratosConsumidos() {
        return carbohidratosConsumidos;
    }

    public void setCarbohidratosConsumidos(Double carbohidratosConsumidos) {
        this.carbohidratosConsumidos = carbohidratosConsumidos;
    }

    public Double getGrasasConsumidas() {
        return grasasConsumidas;
    }

    public void setGrasasConsumidas(Double grasasConsumidas) {
        this.grasasConsumidas = grasasConsumidas;
    }

    public String getTipoComida() {
        return tipoComida;
    }

    public void setTipoComida(String tipoComida) {
        this.tipoComida = tipoComida;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }
}