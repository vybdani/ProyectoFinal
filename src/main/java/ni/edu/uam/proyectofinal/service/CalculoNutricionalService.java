package ni.edu.uam.proyectofinal.service;

import ni.edu.uam.proyectofinal.model.PerfilFisico;
import ni.edu.uam.proyectofinal.model.RequerimientoDiario;
import ni.edu.uam.proyectofinal.model.Usuario;

import java.time.LocalDate;

public class CalculoNutricionalService {

    public RequerimientoDiario calcular(Usuario usuario) {
        if (usuario == null || usuario.getPerfilFisico() == null) {
            throw new IllegalArgumentException("El usuario debe completar primero su perfil físico.");
        }

        PerfilFisico perfil = usuario.getPerfilFisico();
        int edad = perfil.getEdad() != null ? perfil.getEdad() : 0;
        double peso = perfil.getPeso() != null ? perfil.getPeso() : 0.0;
        double altura = perfil.getAltura() != null ? perfil.getAltura() : 0.0;
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

        double tmb;
        if (sexo.equalsIgnoreCase("Masculino")) {
            tmb = (10 * peso) + (6.25 * altura) - (5 * edad) + 5;
        } else if (sexo.equalsIgnoreCase("Femenino")) {
            tmb = (10 * peso) + (6.25 * altura) - (5 * edad) - 161;
        } else {
            throw new IllegalArgumentException("Seleccione una categoría válida para el cálculo.");
        }

        double factor = obtenerFactorActividad(nivel);
        double calorias = tmb * factor;

        double proteinas = (calorias * 0.25) / 4;
        double carbohidratos = (calorias * 0.45) / 4;
        double grasas = (calorias * 0.30) / 9;

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
            throw new IllegalArgumentException("Debe seleccionar el nivel de actividad.");
        }
        return switch (nivel.toLowerCase()) {
            case "sedentario" -> 1.20;
            case "ligero" -> 1.375;
            case "moderado" -> 1.55;
            case "alto" -> 1.725;
            default -> throw new IllegalArgumentException("Nivel de actividad no válido.");
        };
    }
}