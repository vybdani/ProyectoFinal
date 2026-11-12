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
    private String sexo;
    private String nivelActividad;

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

    public PerfilFisico(Double peso, Double altura, Integer edad, Double porcentajeGrasa, String sexo, String nivelActividad) {
        this.peso = peso;
        this.altura = altura;
        this.edad = edad;
        this.porcentajeGrasa = porcentajeGrasa;
        this.sexo = sexo;
        this.nivelActividad = nivelActividad;
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

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public String getNivelActividad() { return nivelActividad; }
    public void setNivelActividad(String nivelActividad) { this.nivelActividad = nivelActividad; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}