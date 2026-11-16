package ni.edu.uam.proyectofinal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "items_lista_super")
public class ItemListaSuper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String nombreProducto;

    private Double cantidad;

    private String unidad;

    private Boolean comprado;

    public ItemListaSuper() {
    }

    public ItemListaSuper(Usuario usuario,
                          String nombreProducto,
                          Double cantidad,
                          String unidad) {

        this.usuario = usuario;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.comprado = false;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public String getUnidad() {
        return unidad;
    }

    public Boolean getComprado() {
        return comprado;
    }

    public void setComprado(Boolean comprado) {
        this.comprado = comprado;
    }
}

