# Guía de Reportes JasperReports para Daniella

Hola Daniella, aquí tienes las plantillas listas y la clase de utilidad `ReporteUtil` para que solo tengas que llamar una línea de código desde tus controladores o botones para generar y exportar los reportes en PDF.

---

## 1. Plantillas Disponibles (en esta misma carpeta)

| Archivo | Para qué sirve | Parámetros que recibe | Campos de la lista |
|---|---|---|---|
| **`ReporteListaSuper.jrxml`** | Lista del supermercado para compras | `USUARIO`, `FECHA` | `alimento`, `cantidad`, `unidad`, `comprado` |
| **`ReporteRutinaGimnasio.jrxml`** | Plan de entrenamiento semanal | `USUARIO`, `OBJETIVO`, `DIAS_SEMANA`, `FECHA` | `diaSemana`, `nombreEjercicio`, `grupoMuscular`, `series`, `repeticiones`, `descansoSegundos` |
| **`ReportePerfilNutricional.jrxml`** | Ficha técnica de perfil y macros | `NOMBRE_USUARIO`, `CORREO`, `PESO`, `ALTURA`, `EDAD`, `SEXO`, `OBJETIVO`, `CALORIAS_OBJETIVO`, `PROTEINAS_G`, `CARBOHIDRATOS_G`, `GRASAS_G`, `FECHA` | No requiere lista (se llena con los parámetros) |

---

## 2. Cómo generar y exportar un reporte en 3 líneas de código

Puedes usar `ReporteUtil` desde cualquier controlador de JavaFX:

### Ejemplo A: Exportar Lista del Súper a PDF
```java
// 1. Parámetros opcionales
Map<String, Object> params = new HashMap<>();
params.put("USUARIO", SesionActual.getUsuarioActivo().getNombre());
params.put("FECHA", LocalDate.now().toString());

// 2. Obtener la lista desde tu DAO o TableView (por ejemplo ItemListaSuperDAO)
List<ItemListaSuper> items = itemListaSuperDAO.listarPorUsuario(usuario.getId());

// 3. Generar y exportar a PDF (o visualizar)
JasperPrint print = ReporteUtil.compilarYLlenar("ReporteListaSuper.jrxml", params, items);
ReporteUtil.exportarPdf(print, "Lista_Super.pdf");
ReporteUtil.abrirPdfConLectorDelSistema("Lista_Super.pdf"); // Abre el visor de PDF de Windows
```

### Ejemplo B: Exportar Rutina de Gimnasio a PDF
```java
Map<String, Object> params = new HashMap<>();
params.put("USUARIO", SesionActual.getUsuarioActivo().getNombre());
params.put("OBJETIVO", "Volumen / Masa Muscular");
params.put("DIAS_SEMANA", 4);

// Pasar la lista de ejercicios de la rutina
JasperPrint print = ReporteUtil.compilarYLlenar("ReporteRutinaGimnasio.jrxml", params, rutinaEjercicios);
ReporteUtil.visualizar(print); // Abre la ventana interactiva de JasperViewer
```

---

## 3. ¿Dónde está la clase de utilidad?
La clase está en:
`ni.edu.uam.proyectofinal.util.ReporteUtil`
Posee métodos para:
- `ReporteUtil.compilarYLlenar(...)`
- `ReporteUtil.exportarPdf(...)`
- `ReporteUtil.visualizar(...)`
- `ReporteUtil.abrirPdfConLectorDelSistema(...)`
