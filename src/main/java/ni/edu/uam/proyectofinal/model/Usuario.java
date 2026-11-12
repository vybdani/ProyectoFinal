package ni.edu.uam.proyectofinal.model;
import jakarta.persistence.*;
@Entity
@Table(name = "usuarios")

public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String nombre;
    @Column(nullable = false, unique = true, length = 100)
    private String correo;
    @Column(nullable = false) private String password;
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PerfilFisico perfilFisico;

    public Usuario() {}

    public Usuario(String nombre, String correo, String password) {
        this.nombre = nombre;
        this.correo = correo;
        this.password = password; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public PerfilFisico getPerfilFisico() { return perfilFisico; }
    public void setPerfilFisico(PerfilFisico perfilFisico) {
        this.perfilFisico = perfilFisico;
        if (perfilFisico != null) {
            perfilFisico.setUsuario(this);
        }
    }
}
