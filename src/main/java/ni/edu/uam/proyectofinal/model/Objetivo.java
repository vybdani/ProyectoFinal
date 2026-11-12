package ni.edu.uam.proyectofinal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "objetivos")
public class Objetivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String modo;

    @Column(nullable = false)
    private String tipo;

    private Integer caloriasDiarias;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public Objetivo() {}

    public Objetivo(String modo, String tipo, Integer caloriasDiarias, Usuario usuario) {
        this.modo = modo;
        this.tipo = tipo;
        this.caloriasDiarias = caloriasDiarias;
        this.usuario = usuario;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getModo() { return modo; }
    public void setModo(String modo) { this.modo = modo; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Integer getCaloriasDiarias() { return caloriasDiarias; }
    public void setCaloriasDiarias(Integer caloriasDiarias) { this.caloriasDiarias = caloriasDiarias; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}