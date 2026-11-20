package ni.edu.uam.proyectofinal.model;

import jakarta.persistence.*;

@Entity
@Table(name = "alimentos")
public class Alimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "categoria", length = 50)
    private String categoria;

    @Column(name = "calorias", nullable = false)
    private Double calorias;

    @Column(name = "proteinas", nullable = false)
    private Double proteinas;

    @Column(name = "carbohidratos", nullable = false)
    private Double carbohidratos;

    @Column(name = "grasas", nullable = false)
    private Double grasas;

    @Column(name = "cantidad_base", nullable = false)
    private Double cantidadBase = 100.0;

    public Alimento() {
    }

    public Alimento(String nombre, String categoria,
                    Double calorias, Double proteinas,
                    Double carbohidratos, Double grasas) {

        this.nombre = nombre;
        this.categoria = categoria;
        this.calorias = calorias;
        this.proteinas = proteinas;
        this.carbohidratos = carbohidratos;
        this.grasas = grasas;
        this.cantidadBase = 100.0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Double getCalorias() {
        return calorias;
    }

    public void setCalorias(Double calorias) {
        this.calorias = calorias;
    }

    public Double getProteinas() {
        return proteinas;
    }

    public void setProteinas(Double proteinas) {
        this.proteinas = proteinas;
    }

    public Double getCarbohidratos() {
        return carbohidratos;
    }

    public void setCarbohidratos(Double carbohidratos) {
        this.carbohidratos = carbohidratos;
    }

    public Double getGrasas() {
        return grasas;
    }

    public void setGrasas(Double grasas) {
        this.grasas = grasas;
    }

    public Double getCantidadBase() {
        return cantidadBase;
    }

    public void setCantidadBase(Double cantidadBase) {
        this.cantidadBase = cantidadBase;
    }

    @Override
    public String toString() {
        return nombre + " (" + calorias + " kcal / " + cantidadBase + "g)";
    }
}