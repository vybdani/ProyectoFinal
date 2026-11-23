# Reportes de Solea en PDF y Excel (JasperReports)

Solea genera tres reportes con **JasperReports 6.21**, y cada uno se puede exportar a **PDF** o a
**Excel (.xlsx)** con la misma plantilla. Las plantillas `.jrxml` están en esta carpeta y los datos
salen de PostgreSQL a través de los DAO de Hibernate.

| Plantilla | Reporte | Dónde se exporta en la app |
|---|---|---|
| `ReporteRutinaGimnasio.jrxml` | Rutina semanal agrupada por día, con series, repeticiones, descanso y total semanal | Rutina de gimnasio → **Vista previa** / **Exportar PDF** / **Exportar Excel** |
| `ReporteListaSuper.jrxml` | Lista del súper (pendientes primero) | Lista del súper → **Vista previa** / **Exportar PDF** / **Exportar Excel** |
| `ReportePerfilNutricional.jrxml` | Ficha con perfil físico, objetivo, calorías y macros del día | Cálculo → **Vista previa** / **Exportar PDF** / **Exportar Excel** |

## Cómo está armado

```
Pantalla (botones Vista previa / Exportar PDF / Exportar Excel)
   ├─ VistaPreviaReporte       llena el reporte en segundo plano y muestra sus páginas en una ventana,
   │                           con zoom y botones para exportar a PDF o Excel desde ahí
   └─ ExportadorReporte        elige dónde guardar y genera el archivo en segundo plano
        └─ ReporteService      reúne los datos con los DAO y arma las filas del reporte
             └─ ReporteUtil    compila la plantilla (una sola vez, queda en caché), la llena,
                               dibuja páginas para la vista previa (renderizarPagina) y
                               exporta a PDF (exportarPdf) o a Excel (exportarXlsx)
```

- **`FormatoReporte`** (`PDF` o `EXCEL`): el formato sale de la extensión del archivo destino
  (`.pdf` o `.xlsx`), así que los métodos de `ReporteService` sirven para los dos.
- **`ReporteService`** (`ni.edu.uam.proyectofinal.reporte`): cada reporte tiene un
  `prepararXxx(...)` que llena la plantilla y devuelve el `JasperPrint` en memoria (lo que muestra
  la vista previa), y un `exportarXxx(...)` que además lo guarda. `exportar(JasperPrint, destino)`
  guarda un reporte ya preparado, por eso exportar desde la vista previa no vuelve a consultar la BD.
  Si faltan datos (sin rutina, lista vacía, sin perfil o sin cálculo) lanza un aviso claro en lugar
  de un archivo vacío.
- **Vista previa**: las páginas se dibujan con `JasperPrintManager.printPageToImage` al doble de
  resolución (se ven nítidas con zoom de 50 % a 200 %, también con Ctrl + / Ctrl -). La imagen se
  pasa a JavaFX con un `PixelWriter`, así que no hace falta el módulo `javafx.swing`.
- **Excel**: todo el reporte queda en una sola hoja, sin filas de relleno, y los números se guardan
  como números (se pueden sumar o filtrar). Lo exporta `JRXlsxExporter`, que viene en JasperReports
  y no necesita dependencias nuevas.
- **Filas del reporte**: `FilaRutinaReporte` y `FilaListaSuperReporte` son JavaBeans con getters,
  porque JasperReports lee los campos `$F{...}` por reflexión. El paquete se exporta en `module-info.java`.
- **`ReporteUtil`** (`ni.edu.uam.proyectofinal.util`): compilar una plantilla tarda segundos, así que
  se compila la primera vez y se reutiliza (los siguientes PDF tardan menos de medio segundo).

## Campos de cada plantilla

**Rutina** · parámetros `USUARIO`, `OBJETIVO`, `DIAS_SEMANA`, `FECHA` ·
campos `diaSemana`, `nombreDia`, `nombreEjercicio`, `grupoMuscular`, `series`, `repeticiones`, `descansoSegundos`.
Los ejercicios se agrupan por día (grupo de Jasper `Dia`) y el pie de la última página muestra el total semanal.

**Lista del súper** · parámetros `TITULO`, `USUARIO`, `FECHA` · campos `alimento`, `cantidad`, `unidad`, `comprado`.

**Ficha nutricional** · solo parámetros: `NOMBRE_USUARIO`, `CORREO`, `PESO`, `ALTURA`, `EDAD`, `SEXO`,
`OBJETIVO`, `CALORIAS_OBJETIVO`, `PROTEINAS_G`, `CARBOHIDRATOS_G`, `GRASAS_G`, `FECHA`.

## Notas

- Colores de la marca Solea: terracota `#B05A36` (gimnasio) y verde oliva `#5B6F00` (nutrición) sobre papel `#F7F5EE`.
- Las fuentes PDF estándar no tienen emojis: los títulos de las plantillas no los usan.
- Excel no pinta los rectángulos que quedan detrás de un texto. Por eso los textos blancos de la
  cabecera llevan `mode="Opaque"` con el mismo color de fondo que la franja; en el PDF se ven igual.
- En modo módulo, JasperReports necesita que `module-info.java` declare `requires` de OpenPDF, ECJ,
  commons-beanutils, commons-digester, commons-collections4 y commons-logging; si falta alguno,
  el PDF falla con `NoClassDefFoundError`.
