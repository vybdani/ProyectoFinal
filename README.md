# NutriFit

Aplicación de escritorio en **JavaFX** que te ayuda a comer (y entrenar) según tu objetivo.
Usa **Hibernate ORM** con **PostgreSQL** para guardar toda la información.

- **Modo Nutrición:** bajar de peso, mantener o subir de peso.
- **Modo Gimnasio:** volumen muscular o definición, con rutina de entrenamiento.

Con tu perfil físico (peso, altura, edad, sexo, nivel de actividad y % de grasa opcional) calcula tus
calorías y macronutrientes diarios, genera un plan semanal de comidas con recetas, arma la lista del
súper y registra lo que vas comiendo.

## Requisitos

| Herramienta | Versión |
|---|---|
| JDK | 21 |
| PostgreSQL | 15 o superior |
| IntelliJ IDEA | Cualquier versión reciente |
| Maven | Incluido en el proyecto (`mvnw`) |

## 1. Preparar la base de datos

1. Abre **pgAdmin** o la consola `psql` con tu usuario de PostgreSQL.
2. Crea la base de datos:

   ```sql
   CREATE DATABASE nutrifit_db;
   ```

3. No hace falta crear las tablas: Hibernate las crea solo la primera vez que se ejecuta la app
   (`hbm2ddl.auto = update`).

## 2. Configurar la conexión (sin subir contraseñas)

La conexión está en `src/main/resources/hibernate.cfg.xml` con la contraseña **vacía**.
**No escribas tu contraseña en ese archivo**: se usa la variable de entorno `DB_PASSWORD`.

| Variable | Para qué sirve | Valor por defecto |
|---|---|---|
| `DB_PASSWORD` | Contraseña de PostgreSQL | *(vacía)* |
| `DB_USER` | Usuario de PostgreSQL | `postgres` |
| `DB_URL` | URL de la base de datos | `jdbc:postgresql://localhost:5432/nutrifit_db` |

**En IntelliJ:** *Run → Edit Configurations… →* selecciona `Launcher` *→ Environment variables* y agrega
`DB_PASSWORD=tu_contraseña`.

**En la terminal (PowerShell):**

```powershell
$env:DB_PASSWORD = "tu_contraseña"
```

## 3. Ejecutar

**Desde IntelliJ:** abre `src/main/java/ni/edu/uam/proyectofinal/Launcher.java` y pulsa ▶ Run.

**Desde la terminal:**

```bash
./mvnw clean javafx:run
```

## Cómo se usa

1. **Registrarse** con nombre, correo y contraseña, y completar el **perfil físico**.
2. En el menú principal elegir **Modo Nutrición** o **Modo Gimnasio**.
3. Elegir y guardar el **objetivo** del modo.
4. **Calcular requerimiento**: calorías, proteínas, carbohidratos y grasas del día.
5. **Recetas**: crear recetas con sus ingredientes (los alimentos básicos se cargan solos).
6. **Plan semanal**: generar el plan automático y enviar los ingredientes a la **lista del súper**.
7. **Registrar consumo**: anotar lo que comiste y compararlo con tu meta del día.
8. **Rutina de gimnasio** (solo en Modo Gimnasio): elegir 3, 4 o 5 días por semana y generar la rutina.
   Volumen usa 4 series de 8-12 repeticiones y Definición 3 series de 12-15 con menos descanso.
   Desde ahí se gestiona el **catálogo de ejercicios** (crear, editar y eliminar).

## Modelo de datos

| Entidad | Tabla | Relaciones |
|---|---|---|
| `Usuario` | `usuarios` | 1:1 `PerfilFisico` |
| `PerfilFisico` | `perfiles_fisicos` | 1:1 `Usuario` |
| `Objetivo` | `objetivos` | N:1 `Usuario` |
| `RequerimientoDiario` | `requerimientos_diarios` | N:1 `Usuario` |
| `Alimento` | `alimentos` | — |
| `RegistroConsumo` | `registros_consumo` | N:1 `Usuario`, N:1 `Alimento` |
| `ItemListaSuper` | `items_lista_super` | N:1 `Usuario` |
| `Receta` | `recetas` | 1:N `RecetaIngrediente` |
| `RecetaIngrediente` | `receta_ingredientes` | N:1 `Receta`, N:1 `Alimento` |
| `PlanComidas` | `planes_comidas` | N:1 `Usuario`, N:1 `RequerimientoDiario`, 1:N `ComidaPlan` |
| `ComidaPlan` | `comidas_plan` | N:1 `PlanComidas`, N:1 `Receta` |
| `GrupoMuscular` | `grupos_musculares` | 1:N `Ejercicio` |
| `Ejercicio` | `ejercicios` | N:1 `GrupoMuscular` |
| `Rutina` | `rutinas` | N:1 `Usuario`, 1:N `RutinaEjercicio` |
| `RutinaEjercicio` | `rutina_ejercicios` | N:1 `Rutina`, N:1 `Ejercicio` |

## Consultas HQL destacadas

| Consulta | Dónde | Entidades relacionadas |
|---|---|---|
| Kcal y macros totales de cada receta a partir de sus ingredientes | `RecetaDAO.obtenerResumenCaloricoRecetas` | `Receta` → `RecetaIngrediente` → `Alimento` |
| Último plan semanal del usuario con sus comidas y recetas | `PlanComidasDAO.buscarUltimoPorUsuario` | `PlanComidas` → `ComidaPlan` → `Receta` |
| Usuario con su perfil físico al iniciar sesión | `UsuarioDAO.autenticar` | `Usuario` → `PerfilFisico` |
| Totales consumidos en el día por usuario | `RegistroConsumoDAO.obtenerTotales` | `RegistroConsumo` → `Usuario` |
| Historial de objetivos con el nombre del usuario | `ObjetivoDAO.listarObjetivosConUsuario` | `Objetivo` → `Usuario` |
| Última rutina del usuario con sus ejercicios y grupos musculares | `RutinaDAO.buscarUltimaPorUsuario` | `Rutina` → `RutinaEjercicio` → `Ejercicio` → `GrupoMuscular` |
| Series semanales por grupo muscular de una rutina | `RutinaDAO.resumenPorGrupoMuscular` | `RutinaEjercicio` → `Ejercicio` → `GrupoMuscular` |
| Ejercicios de un grupo muscular | `EjercicioDAO.listarPorGrupo` | `Ejercicio` → `GrupoMuscular` |

## Estructura del proyecto

```
src/main/java/ni/edu/uam/proyectofinal/
├── controller/   Controladores de las pantallas JavaFX
├── dao/          Acceso a datos con Hibernate (CRUD y consultas HQL)
├── model/        Entidades JPA
├── service/      Lógica de negocio (cálculo nutricional, generador de plan y de rutina)
└── util/         HibernateUtil, sesión del usuario y navegación
src/main/resources/
├── hibernate.cfg.xml
└── ni/edu/uam/proyectofinal/views/   Vistas FXML
```

## Equipo y flujo de trabajo

| Integrante | Módulo |
|---|---|
| Luis Aburto | Base del proyecto, usuarios, perfil y menú |
| Daris Mayorga | Alimentos, cálculo nutricional, consumo y lista del súper |
| Franklin Callejas | Recetas y plan semanal de comidas |
| Daniella Rocha | Modo gimnasio e integración |

- `main` siempre estable; cada funcionalidad se trabaja en su propia rama.
- Los cambios entran a `main` mediante **Pull Request** revisado por otro integrante.
- Commits pequeños con mensajes descriptivos (`feat:`, `fix:`, `docs:`, `refactor:`).
