package ni.edu.uam.proyectofinal.model;

/**
 * Representa un consejo o recomendación nutricional obtenido desde la API REST.
 */
public class ConsejoNutricional {

    private String objetivo;
    private String titulo;
    private String consejo;
    private String distribucionMacrosSugerida;
    private String recomendacionAgua;
    private String fuente;

    public ConsejoNutricional() {}

    public ConsejoNutricional(String objetivo, String titulo, String consejo,
                              String distribucionMacrosSugerida, String recomendacionAgua, String fuente) {
        this.objetivo = objetivo;
        this.titulo = titulo;
        this.consejo = consejo;
        this.distribucionMacrosSugerida = distribucionMacrosSugerida;
        this.recomendacionAgua = recomendacionAgua;
        this.fuente = fuente;
    }

    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getConsejo() { return consejo; }
    public void setConsejo(String consejo) { this.consejo = consejo; }

    public String getDistribucionMacrosSugerida() { return distribucionMacrosSugerida; }
    public void setDistribucionMacrosSugerida(String distribucionMacrosSugerida) { this.distribucionMacrosSugerida = distribucionMacrosSugerida; }

    public String getRecomendacionAgua() { return recomendacionAgua; }
    public void setRecomendacionAgua(String recomendacionAgua) { this.recomendacionAgua = recomendacionAgua; }

    public String getFuente() { return fuente; }
    public void setFuente(String fuente) { this.fuente = fuente; }
}
