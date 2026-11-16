package ni.edu.uam.proyectofinal.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recetas")
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 40)
    private String tipoComida;

    @Column(length = 500)
    private String descripcion;

    private Integer tiempoPreparacionMin;

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RecetaIngrediente> ingredientes = new ArrayList<>();

    public Receta() {
    }

    public Receta(String nombre, String tipoComida, String descripcion, Integer tiempoPreparacionMin) {
        this.nombre = nombre;
        this.tipoComida = tipoComida;
        this.descripcion = descripcion;
        this.tiempoPreparacionMin = tiempoPreparacionMin;
    }

    public void agregarIngrediente(RecetaIngrediente ingrediente) {
        ingredientes.add(ingrediente);
        ingrediente.setReceta(this);
    }

    public void removerIngrediente(RecetaIngrediente ingrediente) {
        ingredientes.remove(ingrediente);
        ingrediente.setReceta(null);
    }

    public double getCaloriasTotales() {
        return ingredientes.stream().mapToDouble(RecetaIngrediente::getCalorias).sum();
    }

    public double getProteinasTotales() {
        return ingredientes.stream().mapToDouble(RecetaIngrediente::getProteinas).sum();
    }

    public double getCarbohidratosTotales() {
        return ingredientes.stream().mapToDouble(RecetaIngrediente::getCarbohidratos).sum();
    }

    public double getGrasasTotales() {
        return ingredientes.stream().mapToDouble(RecetaIngrediente::getGrasas).sum();
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

    public String getTipoComida() {
        return tipoComida;
    }

    public void setTipoComida(String tipoComida) {
        this.tipoComida = tipoComida;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getTiempoPreparacionMin() {
        return tiempoPreparacionMin;
    }

    public void setTiempoPreparacionMin(Integer tiempoPreparacionMin) {
        this.tiempoPreparacionMin = tiempoPreparacionMin;
    }

    public List<RecetaIngrediente> getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(List<RecetaIngrediente> ingredientes) {
        this.ingredientes = ingredientes;
    }

    @Override
    public String toString() {
        return nombre + " (" + String.format("%.0f", getCaloriasTotales()) + " kcal)";
    }
}