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
    @Column(nullable = false, length = 255)
    private String password;

    /** Pregunta para recuperar la cuenta si se olvida la contraseña. */
    @Column(name = "pregunta_seguridad", length = 150)
    private String preguntaSeguridad;

    /** Respuesta a la pregunta de seguridad, guardada como hash (nunca en texto plano). */
    @Column(name = "respuesta_seguridad", length = 255)
    private String respuestaSeguridad;

    /** true cuando el usuario ya vio el tutorial de bienvenida (null en cuentas antiguas = no lo vio). */
    @Column(name = "tutorial_visto")
    private Boolean tutorialVisto = false;

    /** El perfil pertenece al usuario: se guarda y se borra junto con él (cascade ALL). */
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

    public String getPreguntaSeguridad() { return preguntaSeguridad; }
    public void setPreguntaSeguridad(String preguntaSeguridad) { this.preguntaSeguridad = preguntaSeguridad; }

    public String getRespuestaSeguridad() { return respuestaSeguridad; }
    public void setRespuestaSeguridad(String respuestaSeguridad) { this.respuestaSeguridad = respuestaSeguridad; }

    /** true si el usuario ya vio el tutorial de bienvenida. */
    public boolean isTutorialVisto() { return Boolean.TRUE.equals(tutorialVisto); }
    public void setTutorialVisto(Boolean tutorialVisto) { this.tutorialVisto = tutorialVisto; }

    public PerfilFisico getPerfilFisico() { return perfilFisico; }
    public void setPerfilFisico(PerfilFisico perfilFisico) {
        this.perfilFisico = perfilFisico;
        if (perfilFisico != null) {
            perfilFisico.setUsuario(this);
        }
    }
}
