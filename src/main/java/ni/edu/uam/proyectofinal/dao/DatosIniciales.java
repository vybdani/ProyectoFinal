package ni.edu.uam.proyectofinal.dao;

import java.util.List;

/**
 * Alimentos y recetas que trae Solea desde el primer uso, para que el usuario pueda registrar
 * su consumo y generar su plan semanal sin tener que cargar nada a mano.
 *
 * <p>Los valores nutricionales son aproximados por cada 100 g (tablas de composición de alimentos).
 * Se insertan solo los que faltan, comparando por nombre, así que no se duplican.</p>
 */
public final class DatosIniciales {

    private DatosIniciales() {
    }

    /** Alimento base: nombre, categoría y kcal / proteínas / carbohidratos / grasas por 100 g. */
    public record AlimentoBase(String nombre, String categoria, double calorias, double proteinas,
                               double carbohidratos, double grasas) {
    }

    /** Ingrediente de una receta: nombre del alimento (de la lista de abajo) y gramos. */
    public record IngredienteBase(String alimento, double gramos) {
    }

    /** Receta base con su tipo de comida (Desayuno, Almuerzo, Cena o Merienda). */
    public record RecetaBase(String nombre, String tipoComida, int minutos, String descripcion,
                             List<IngredienteBase> ingredientes) {
    }

    public static final List<AlimentoBase> ALIMENTOS = List.of(
            // Proteínas
            new AlimentoBase("Pechuga de Pollo", "Proteína", 165.0, 31.0, 0.0, 3.6),
            new AlimentoBase("Muslo de Pollo sin Piel", "Proteína", 209.0, 26.0, 0.0, 10.9),
            new AlimentoBase("Huevo Entero", "Proteína", 155.0, 13.0, 1.1, 11.0),
            new AlimentoBase("Clara de Huevo", "Proteína", 52.0, 10.9, 0.7, 0.2),
            new AlimentoBase("Atún en Agua", "Proteína", 116.0, 26.0, 0.0, 1.0),
            new AlimentoBase("Sardinas en Aceite", "Proteína", 208.0, 24.6, 0.0, 11.5),
            new AlimentoBase("Filete de Tilapia", "Proteína", 128.0, 26.2, 0.0, 2.7),
            new AlimentoBase("Carne de Res Magra", "Proteína", 217.0, 26.0, 0.0, 12.0),
            new AlimentoBase("Lomo de Cerdo", "Proteína", 143.0, 26.0, 0.0, 3.5),
            new AlimentoBase("Jamón de Pavo", "Proteína", 104.0, 17.0, 4.2, 1.7),
            new AlimentoBase("Proteína de Suero (Whey)", "Proteína", 400.0, 80.0, 8.0, 6.0),
            // Legumbres y platos típicos
            new AlimentoBase("Frijoles Rojos Cocidos", "Legumbre", 127.0, 8.7, 22.8, 0.5),
            new AlimentoBase("Frijoles Negros Cocidos", "Legumbre", 132.0, 8.9, 23.7, 0.5),
            new AlimentoBase("Lentejas Cocidas", "Legumbre", 116.0, 9.0, 20.1, 0.4),
            new AlimentoBase("Garbanzos Cocidos", "Legumbre", 164.0, 8.9, 27.4, 2.6),
            new AlimentoBase("Gallo Pinto", "Plato típico", 160.0, 5.0, 27.0, 3.5),
            // Carbohidratos
            new AlimentoBase("Arroz Blanco Cocido", "Carbohidrato", 130.0, 2.7, 28.0, 0.3),
            new AlimentoBase("Arroz Integral Cocido", "Carbohidrato", 123.0, 2.7, 25.6, 1.0),
            new AlimentoBase("Avena en Hojuelas", "Carbohidrato", 389.0, 16.9, 66.3, 6.9),
            new AlimentoBase("Pan Integral", "Carbohidrato", 247.0, 13.0, 41.0, 3.4),
            new AlimentoBase("Pan Blanco", "Carbohidrato", 265.0, 9.0, 49.0, 3.2),
            new AlimentoBase("Pasta Cocida", "Carbohidrato", 158.0, 5.8, 30.9, 0.9),
            new AlimentoBase("Papa Cocida", "Carbohidrato", 87.0, 1.9, 20.1, 0.1),
            new AlimentoBase("Camote Cocido", "Carbohidrato", 76.0, 1.4, 17.7, 0.1),
            new AlimentoBase("Yuca Cocida", "Carbohidrato", 112.0, 1.4, 26.9, 0.3),
            new AlimentoBase("Plátano Maduro", "Carbohidrato", 122.0, 1.3, 31.9, 0.4),
            new AlimentoBase("Plátano Verde Cocido", "Carbohidrato", 116.0, 0.8, 31.2, 0.2),
            new AlimentoBase("Tortilla de Maíz", "Carbohidrato", 218.0, 5.7, 44.6, 2.9),
            new AlimentoBase("Granola", "Carbohidrato", 471.0, 10.0, 64.0, 20.0),
            new AlimentoBase("Galletas Soda", "Carbohidrato", 421.0, 9.5, 71.0, 10.0),
            new AlimentoBase("Cereal de Maíz", "Carbohidrato", 357.0, 7.5, 84.0, 0.4),
            // Lácteos
            new AlimentoBase("Leche Descremada", "Lácteo", 34.0, 3.4, 5.0, 0.1),
            new AlimentoBase("Leche Entera", "Lácteo", 61.0, 3.2, 4.8, 3.3),
            new AlimentoBase("Yogur Griego Natural", "Lácteo", 59.0, 10.0, 3.6, 0.4),
            new AlimentoBase("Yogur Natural", "Lácteo", 61.0, 3.5, 4.7, 3.3),
            new AlimentoBase("Queso Fresco", "Lácteo", 264.0, 18.0, 3.0, 20.0),
            new AlimentoBase("Queso Mozzarella", "Lácteo", 280.0, 28.0, 3.1, 17.0),
            new AlimentoBase("Crema Ácida", "Lácteo", 193.0, 2.4, 4.6, 19.0),
            // Grasas
            new AlimentoBase("Aguacate", "Grasa", 160.0, 2.0, 8.5, 14.7),
            new AlimentoBase("Maní", "Grasa", 567.0, 25.8, 16.1, 49.2),
            new AlimentoBase("Mantequilla de Maní", "Grasa", 588.0, 25.0, 20.0, 50.0),
            new AlimentoBase("Almendras", "Grasa", 579.0, 21.0, 22.0, 50.0),
            new AlimentoBase("Semillas de Chía", "Grasa", 486.0, 17.0, 42.0, 31.0),
            new AlimentoBase("Aceite de Oliva", "Grasa", 884.0, 0.0, 0.0, 100.0),
            new AlimentoBase("Aceite Vegetal", "Grasa", 884.0, 0.0, 0.0, 100.0),
            new AlimentoBase("Mantequilla", "Grasa", 717.0, 0.9, 0.1, 81.0),
            // Frutas
            new AlimentoBase("Banano", "Fruta", 89.0, 1.1, 22.8, 0.3),
            new AlimentoBase("Manzana", "Fruta", 52.0, 0.3, 13.8, 0.2),
            new AlimentoBase("Naranja", "Fruta", 47.0, 0.9, 11.8, 0.1),
            new AlimentoBase("Papaya", "Fruta", 43.0, 0.5, 10.8, 0.3),
            new AlimentoBase("Piña", "Fruta", 50.0, 0.5, 13.1, 0.1),
            new AlimentoBase("Mango", "Fruta", 60.0, 0.8, 15.0, 0.4),
            new AlimentoBase("Sandía", "Fruta", 30.0, 0.6, 7.6, 0.2),
            new AlimentoBase("Fresas", "Fruta", 32.0, 0.7, 7.7, 0.3),
            // Verduras
            new AlimentoBase("Brócoli", "Verdura", 34.0, 2.8, 6.6, 0.4),
            new AlimentoBase("Tomate", "Verdura", 18.0, 0.9, 3.9, 0.2),
            new AlimentoBase("Zanahoria", "Verdura", 41.0, 0.9, 9.6, 0.2),
            new AlimentoBase("Lechuga", "Verdura", 15.0, 1.4, 2.9, 0.2),
            new AlimentoBase("Pepino", "Verdura", 15.0, 0.7, 3.6, 0.1),
            new AlimentoBase("Cebolla", "Verdura", 40.0, 1.1, 9.3, 0.1),
            new AlimentoBase("Chile Dulce", "Verdura", 31.0, 1.0, 6.0, 0.3),
            new AlimentoBase("Repollo", "Verdura", 25.0, 1.3, 5.8, 0.1),
            new AlimentoBase("Espinaca", "Verdura", 23.0, 2.9, 3.6, 0.4),
            new AlimentoBase("Ayote", "Verdura", 26.0, 1.0, 6.5, 0.1),
            // Otros
            new AlimentoBase("Miel de Abeja", "Otro", 304.0, 0.3, 82.0, 0.0),
            new AlimentoBase("Azúcar", "Otro", 387.0, 0.0, 100.0, 0.0)
    );

    public static final List<RecetaBase> RECETAS = List.of(
            // Desayuno
            receta("Gallo pinto con huevo y queso", "Desayuno", 15,
                    "Calienta el gallo pinto, acompaña con huevo revuelto, queso fresco y tortilla.",
                    "Gallo Pinto", 150, "Huevo Entero", 100, "Queso Fresco", 30, "Tortilla de Maíz", 50),
            receta("Avena con banano y miel", "Desayuno", 10,
                    "Cocina la avena en la leche y sirve con banano en rodajas y un hilo de miel.",
                    "Avena en Hojuelas", 60, "Leche Descremada", 250, "Banano", 120, "Miel de Abeja", 10),
            receta("Tostadas con huevo y aguacate", "Desayuno", 10,
                    "Tuesta el pan, agrega aguacate machacado, huevo y rodajas de tomate.",
                    "Pan Integral", 60, "Huevo Entero", 100, "Aguacate", 50, "Tomate", 50),
            receta("Yogur griego con fresas y granola", "Desayuno", 5,
                    "Sirve el yogur con fresas picadas y granola por encima.",
                    "Yogur Griego Natural", 200, "Fresas", 100, "Granola", 30),
            // Almuerzo
            receta("Pollo a la plancha con arroz y ensalada", "Almuerzo", 25,
                    "Cocina el pollo a la plancha y acompaña con arroz y ensalada de lechuga y tomate.",
                    "Pechuga de Pollo", 150, "Arroz Blanco Cocido", 150, "Lechuga", 50, "Tomate", 50,
                    "Aceite de Oliva", 5),
            receta("Carne asada con gallo pinto y maduro", "Almuerzo", 30,
                    "Asa la carne y sirve con gallo pinto, plátano maduro y ensalada de repollo.",
                    "Carne de Res Magra", 150, "Gallo Pinto", 150, "Plátano Maduro", 100, "Repollo", 50),
            receta("Tilapia con arroz integral y verduras", "Almuerzo", 25,
                    "Cocina la tilapia a la plancha y acompaña con arroz integral, brócoli y zanahoria al vapor.",
                    "Filete de Tilapia", 150, "Arroz Integral Cocido", 150, "Brócoli", 100, "Zanahoria", 50,
                    "Aceite de Oliva", 5),
            receta("Pasta con atún y tomate", "Almuerzo", 20,
                    "Mezcla la pasta con el atún escurrido y una salsa sencilla de tomate.",
                    "Pasta Cocida", 200, "Atún en Agua", 100, "Tomate", 100, "Aceite de Oliva", 5),
            // Cena
            receta("Tortillas con frijoles y queso", "Cena", 10,
                    "Calienta las tortillas y rellena con frijoles, queso fresco y un poco de crema.",
                    "Tortilla de Maíz", 100, "Frijoles Rojos Cocidos", 120, "Queso Fresco", 40, "Crema Ácida", 20),
            receta("Ensalada de pollo y aguacate", "Cena", 15,
                    "Mezcla el pollo en tiras con lechuga, aguacate, pepino y tomate.",
                    "Pechuga de Pollo", 120, "Lechuga", 80, "Aguacate", 60, "Pepino", 50, "Tomate", 50,
                    "Aceite de Oliva", 5),
            receta("Omelette de claras con espinaca", "Cena", 10,
                    "Bate las claras, cocina con espinaca y mozzarella; acompaña con pan integral.",
                    "Clara de Huevo", 200, "Espinaca", 60, "Queso Mozzarella", 30, "Pan Integral", 40),
            receta("Sopa de lentejas con verduras", "Cena", 35,
                    "Cocina las lentejas con zanahoria, cebolla y papa hasta que espese.",
                    "Lentejas Cocidas", 200, "Zanahoria", 50, "Cebolla", 30, "Papa Cocida", 100),
            // Merienda
            receta("Batido de proteína con banano", "Merienda", 5,
                    "Licúa la proteína con leche, banano y mantequilla de maní.",
                    "Proteína de Suero (Whey)", 30, "Leche Descremada", 250, "Banano", 100,
                    "Mantequilla de Maní", 15),
            receta("Manzana con mantequilla de maní", "Merienda", 2,
                    "Corta la manzana en gajos y acompaña con mantequilla de maní.",
                    "Manzana", 150, "Mantequilla de Maní", 20),
            receta("Fruta picada tropical", "Merienda", 5,
                    "Pica papaya, piña y sandía en cubos.",
                    "Papaya", 100, "Piña", 100, "Sandía", 100),
            receta("Puñado de maní y almendras", "Merienda", 1,
                    "Una porción pequeña de frutos secos.",
                    "Maní", 20, "Almendras", 20)
    );

    /** receta("Nombre", "Tipo", minutos, "Descripción", "Alimento", gramos, "Alimento", gramos, ...) */
    private static RecetaBase receta(String nombre, String tipo, int minutos, String descripcion, Object... pares) {
        IngredienteBase[] ingredientes = new IngredienteBase[pares.length / 2];
        for (int i = 0; i < pares.length; i += 2) {
            ingredientes[i / 2] = new IngredienteBase((String) pares[i], ((Number) pares[i + 1]).doubleValue());
        }
        return new RecetaBase(nombre, tipo, minutos, descripcion, List.of(ingredientes));
    }
}
