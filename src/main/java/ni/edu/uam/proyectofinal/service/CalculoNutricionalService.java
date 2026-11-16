package ni.edu.uam.proyectofinal.service;

import ni.edu.uam.proyectofinal.model.Objetivo;
import ni.edu.uam.proyectofinal.model.PerfilFisico;
import ni.edu.uam.proyectofinal.model.RequerimientoDiario;
import ni.edu.uam.proyectofinal.model.Usuario;

import java.time.LocalDate;
import java.util.List;

public class CalculoNutricionalService {

    // Objetivos de cada modo (se guardan tal cual en la columna "tipo" de Objetivo)
    public static final String BAJAR_PESO = "Bajar de peso";
    public static final String MANTENER_PESO = "Mantener peso";
    public static final String SUBIR_PESO = "Subir de peso";
    public static final String VOLUMEN = "Volumen muscular";
    public static final String DEFINICION = "Definición";

    public static final List<String> OBJETIVOS_NUTRICION = List.of(BAJAR_PESO, MANTENER_PESO, SUBIR_PESO);
    public static final List<String> OBJETIVOS_GIMNASIO = List.of(VOLUMEN, DEFINICION);

    /** Calcula sin objetivo: se asume mantener el peso. */
    public RequerimientoDiario calcular(Usuario usuario) {
        return calcular(usuario, null);
    }

    public RequerimientoDiario calcular(Usuario usuario, Objetivo objetivo) {
        if (usuario == null || usuario.getPerfilFisico() == null) {
            throw new IllegalArgumentException("El usuario debe completar primero su perfil físico.");
        }

        PerfilFisico perfil = usuario.getPerfilFisico();
        int edad = perfil.getEdad() != null ? perfil.getEdad() : 0;
        double peso = perfil.getPeso() != null ? perfil.getPeso() : 0.0;
        double altura = perfil.getAltura() != null ? perfil.getAltura() : 0.0;
        double grasa = perfil.getPorcentajeGrasa() != null ? perfil.getPorcentajeGrasa() : 0.0;
        String sexo = perfil.getSexo();
        String nivel = perfil.getNivelActividad();

        if (edad < 18) {
            throw new IllegalArgumentException("Se requiere evaluación profesional para menores de edad.");
        }
        if (peso <= 0 || altura <= 0) {
            throw new IllegalArgumentException("El peso y la altura deben ser positivos.");
        }
        if (sexo == null || sexo.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar el sexo en su perfil físico.");
        }

        double tmb = calcularTmb(sexo, peso, altura, edad, grasa);
        double factor = obtenerFactorActividad(nivel);
        double caloriasMantenimiento = tmb * factor;

        String tipo = objetivo != null ? objetivo.getTipo() : MANTENER_PESO;
        double calorias = caloriasMantenimiento * (1 + ajusteCalorico(tipo));

        // Mínimo seguro para no recomendar dietas demasiado bajas
        double minimo = sexo.equalsIgnoreCase("Femenino") ? 1200 : 1500;
        calorias = Math.max(calorias, minimo);

        // Proteína según peso corporal y objetivo; grasa 25 % de las kcal; el resto carbohidratos
        double proteinas = proteinaPorKilo(tipo) * peso;
        double grasas = (calorias * 0.25) / 9;
        double carbohidratos = Math.max(0, (calorias - proteinas * 4 - grasas * 9) / 4);

        return new RequerimientoDiario(
                redondear(tmb),
                factor,
                redondear(calorias),
                redondear(proteinas),
                redondear(carbohidratos),
                redondear(grasas),
                LocalDate.now(),
                usuario
        );
    }

    /**
     * Si el usuario indicó su % de grasa se usa Katch-McArdle (basada en masa magra),
     * si no, Mifflin-St Jeor.
     */
    private double calcularTmb(String sexo, double peso, double altura, int edad, double grasa) {
        if (grasa > 0 && grasa < 60) {
            double masaMagra = peso * (1 - grasa / 100);
            return 370 + 21.6 * masaMagra;
        }
        if (sexo.equalsIgnoreCase("Masculino")) {
            return (10 * peso) + (6.25 * altura) - (5 * edad) + 5;
        } else if (sexo.equalsIgnoreCase("Femenino")) {
            return (10 * peso) + (6.25 * altura) - (5 * edad) - 161;
        }
        throw new IllegalArgumentException("Seleccione una categoría válida para el cálculo.");
    }

    /** Porcentaje que se suma o resta a las calorías de mantenimiento. */
    public double ajusteCalorico(String tipo) {
        if (tipo == null) {
            return 0;
        }
        return switch (tipo) {
            case BAJAR_PESO -> -0.20;
            case SUBIR_PESO -> 0.10;
            case VOLUMEN -> 0.12;
            case DEFINICION -> -0.20;
            default -> 0;
        };
    }

    /** Gramos de proteína por kg de peso corporal. */
    public double proteinaPorKilo(String tipo) {
        if (tipo == null) {
            return 1.6;
        }
        return switch (tipo) {
            case BAJAR_PESO, SUBIR_PESO -> 1.8;
            case VOLUMEN -> 2.0;
            case DEFINICION -> 2.2;
            default -> 1.6;
        };
    }

    /** Texto corto que explica qué hace cada objetivo, para mostrarlo en pantalla. */
    public String descripcion(String tipo) {
        if (tipo == null) {
            return "";
        }
        return switch (tipo) {
            case BAJAR_PESO -> "Déficit de 20 % de calorías para bajar grasa de forma gradual.";
            case MANTENER_PESO -> "Calorías de mantenimiento para conservar tu peso actual.";
            case SUBIR_PESO -> "Superávit de 10 % de calorías para subir de peso.";
            case VOLUMEN -> "Superávit de 12 % y 2 g de proteína por kg para ganar músculo.";
            case DEFINICION -> "Déficit de 20 % y 2.2 g de proteína por kg para bajar grasa sin perder músculo.";
            default -> "";
        };
    }

    private double obtenerFactorActividad(String nivel) {
        if (nivel == null) {
            throw new IllegalArgumentException("Debe seleccionar el nivel de actividad en su perfil físico.");
        }
        return switch (nivel.toLowerCase()) {
            case "sedentario" -> 1.20;
            case "ligero" -> 1.375;
            case "moderado" -> 1.55;
            case "alto" -> 1.725;
            default -> throw new IllegalArgumentException("Nivel de actividad no válido.");
        };
    }

    private double redondear(double valor) {
        return Math.round(valor * 10) / 10.0;
    }
}
