# Reportes PDF de Solea (JasperReports)

Solea genera tres reportes en PDF con **JasperReports 6.21**. Las plantillas `.jrxml` están en esta
carpeta y los datos salen de PostgreSQL a través de los DAO de Hibernate.

| Plantilla | Reporte | Dónde se exporta en la app |
|---|---|---|
| `ReporteRutinaGimnasio.jrxml` | Rutina semanal agrupada por día, con series, repeticiones, descanso y total semanal | Rutina de gimnasio → **Exportar PDF** |
| `ReporteListaSuper.jrxml` | Lista del súper (pendientes primero) | Lista del súper → **Exportar PDF** |
| `ReportePerfilNutricional.jrxml` | Ficha con perfil físico, objetivo, calorías y macros del día | Cálculo → **Exportar PDF** |

## Cómo está armado

```
Pantalla (botón Exportar PDF)
   └─ ExportadorPdf            elige dónde guardar y genera el PDF en segundo plano
        └─ ReporteService      reúne los datos con los DAO y arma las filas del reporte
             └─ ReporteUtil    compila la plantilla (una sola vez, queda en caché), la llena y exporta a PDF
```

- **`ReporteService`** (`ni.edu.uam.proyectofinal.reporte`): un método por reporte
  (`exportarRutina`, `exportarListaSuper`, `exportarPerfilNutricional`). Si faltan datos
  (sin rutina, lista vacía, sin perfil o sin cálculo) lanza un aviso claro en lugar de un PDF vacío.
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
- En modo módulo, JasperReports necesita que `module-info.java` declare `requires` de OpenPDF, ECJ,
  commons-beanutils, commons-digester, commons-collections4 y commons-logging; si falta alguno,
  el PDF falla con `NoClassDefFoundError`.
