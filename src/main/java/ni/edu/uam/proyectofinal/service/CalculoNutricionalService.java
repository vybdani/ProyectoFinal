
package ni.edu.uam.proyectofinal.service;

import ni.edu.uam.nutrifit.entity.Usuario;
import ni.edu.uam.proyectofinal.model.RequerimientoDiario;

import java.time.LocalDate;

public class CalculoNutricionalService {

    public RequerimientoDiario calcular(ni.edu.uam.nutrifit.service.Usuario usuario) {

        int edad = usuario.getEdad();
        double peso = usuario.getPeso();
        double altura = usuario.getAltura();

        String sexo = usuario.getSexo();
        String nivel = usuario.getNivelActividad();

        if (edad < 18) {
            throw new IllegalArgumentException(
                    "Se requiere evaluación profesional para menores de edad."
            );
        }

        if (peso <= 0 || altura <= 0) {
            throw new IllegalArgumentException(
                    "El peso y la altura deben ser positivos."
            );
        }

        // 1. Calcular TMB
        double tmb;

        if (sexo.equalsIgnoreCase("Masculino")) {

            tmb = (10 * peso) + (6.25 * altura)
                    - (5 * edad) + 5;

        } else if (sexo.equalsIgnoreCase("Femenino")) {

            tmb = (10 * peso) + (6.25 * altura)
                    - (5 * edad) - 161;

        } else {

            throw new IllegalArgumentException(
                    "Seleccione una categoría válida para el cálculo."
            );
        }

        // 2. Obtener factor de actividad
        double factor = obtenerFactorActividad(nivel);

        // 3. Estimación de mantenimiento
        double calorias = tmb * factor;

        // 4. Distribución ilustrativa de macronutrientes
        double proteinas = (calorias * 0.20) / 4;
        double carbohidratos = (calorias * 0.50) / 4;
        double grasas = (calorias * 0.30) / 9;

        // 5. Crear objeto de resultado
        return new RequerimientoDiario(
                tmb,
                factor,
                calorias,
                proteinas,
                carbohidratos,
                grasas,
                LocalDate.now(),
                usuario
        );
    }

    private double obtenerFactorActividad(String nivel) {

        if (nivel == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar el nivel de actividad."
            );
        }

        switch (nivel.toLowerCase()) {

            case "sedentario":
                return 1.20;

            case "ligero":
                return 1.375;

            case "moderado":
                return 1.55;

            case "alto":
                return 1.725;

            default:
                throw new IllegalArgumentException(
                        "Nivel de actividad no válido."
                );
        }
    }
}
