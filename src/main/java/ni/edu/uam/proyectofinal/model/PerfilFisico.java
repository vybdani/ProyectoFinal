package ni.edu.uam.proyectofinal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "perfiles_fisicos")
public class PerfilFisico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double peso;
    private Double altura;
    private Integer edad;
    private Double porcentajeGrasa;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    public PerfilFisico() {}

    public PerfilFisico(Double peso, Double altura, Integer edad, Double porcentajeGrasa) {
        this.peso = peso;
        this.altura = altura;
        this.edad = edad;
        this.porcentajeGrasa = porcentajeGrasa;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getPeso() { return peso; }
    public void setPeso(Double peso) { this.peso = peso; }

    public Double getAltura() { return altura; }
    public void setAltura(Double altura) { this.altura = altura; }

    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }

    public Double getPorcentajeGrasa() { return porcentajeGrasa; }
    public void setPorcentajeGrasa(Double porcentajeGrasa) { this.porcentajeGrasa = porcentajeGrasa; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}