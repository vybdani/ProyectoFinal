package ni.edu.uam.proyectofinal.model;

/**
 * Representa un alimento obtenido desde la API REST (respaldada por Open Food Facts).
 */
public class AlimentoExterno {

    private String id;
    private String nombre;
    private String marca;
    private Double caloriasPor100g;
    private Double proteinasPor100g;
    private Double carbohidratosPor100g;
    private Double grasasPor100g;
    private String categoria;
    private String imagenUrl;
    private String fuente;

    public AlimentoExterno() {}

    public AlimentoExterno(String id, String nombre, String marca, Double caloriasPor100g,
                           Double proteinasPor100g, Double carbohidratosPor100g,
                           Double grasasPor100g, String categoria, String imagenUrl, String fuente) {
        this.id = id;
        this.nombre = nombre;
        this.marca = marca;
        this.caloriasPor100g = caloriasPor100g;
        this.proteinasPor100g = proteinasPor100g;
        this.carbohidratosPor100g = carbohidratosPor100g;
        this.grasasPor100g = grasasPor100g;
        this.categoria = categoria;
        this.imagenUrl = imagenUrl;
        this.fuente = fuente;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public Double getCaloriasPor100g() { return caloriasPor100g; }
    public void setCaloriasPor100g(Double caloriasPor100g) { this.caloriasPor100g = caloriasPor100g; }

    public Double getProteinasPor100g() { return proteinasPor100g; }
    public void setProteinasPor100g(Double proteinasPor100g) { this.proteinasPor100g = proteinasPor100g; }

    public Double getCarbohidratosPor100g() { return carbohidratosPor100g; }
    public void setCarbohidratosPor100g(Double carbohidratosPor100g) { this.carbohidratosPor100g = carbohidratosPor100g; }

    public Double getGrasasPor100g() { return grasasPor100g; }
    public void setGrasasPor100g(Double grasasPor100g) { this.grasasPor100g = grasasPor100g; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public String getFuente() { return fuente; }
    public void setFuente(String fuente) { this.fuente = fuente; }

    @Override
    public String toString() {
        return nombre + " (" + marca + ") - " + caloriasPor100g + " kcal";
    }
}
