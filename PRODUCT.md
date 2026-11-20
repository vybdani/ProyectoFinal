# Product

<!-- impeccable:product-schema 1 -->

## Platform

desktop

Aplicación de escritorio en JavaFX 21 + FXML (no es web, iOS ni Android). Seguir en JavaFX es un compromiso confirmado: los rediseños se hacen con FXML y CSS de JavaFX, sin migrar a web.

## Users

Personas que entrenan y quieren cuidar su alimentación y su rutina de gimnasio según un objetivo concreto (bajar, mantener o subir de peso; volumen o definición). La usarían a diario en su computadora para planificar la semana, hacer las compras y registrar lo que comen.

Contexto secundario (deducido del repositorio, no es el público objetivo): es el proyecto final de un equipo de la UAM (Luis Aburto, Daris Mayorga, Franklin Callejas y Daniella Rocha).

## Product Purpose

Solea convierte el perfil físico de una persona (peso, altura, edad, sexo, nivel de actividad y % de grasa opcional) en un plan accionable: calorías y macros diarios, plan semanal de comidas con recetas, lista del súper y registro de consumo comparado con la meta del día. En Modo Gimnasio también genera una rutina de 3, 4 o 5 días por semana.

El éxito es que el usuario siga el plan día a día sin cambiar de herramienta.

## Positioning

Todo en un mismo flujo: perfil → objetivo → requerimiento → recetas → plan semanal → lista del súper → registro de consumo (y rutina en Modo Gimnasio). Cada paso alimenta al siguiente con los datos que ya generó. Ese encadenamiento es lo que la distingue de las apps que solo cuentan calorías o solo arman rutinas.

## Operating Context

- Sesión con cuenta (registro con nombre, correo y contraseña) y perfil físico.
- El menú principal pide elegir **Modo Nutrición** o **Modo Gimnasio**; cada modo tiene sus propios objetivos.
- Datos guardados en PostgreSQL mediante Hibernate (base `solea_db`).
- API REST de apoyo (Spring Boot, `api/`): consejos por objetivo, cálculo de macros y búsqueda de alimentos reales en Open Food Facts.
- Reportes en PDF con JasperReports: lista del súper, perfil nutricional y rutina de gimnasio.

## Capabilities and Constraints

- Pantallas actuales (FXML): login, perfil físico, menú principal, modo/objetivo, cálculo de requerimiento, recetas, plan semanal, lista del súper, consumo, rutina y catálogo de ejercicios.
- Rutina: Volumen = 4 series de 8-12 repeticiones; Definición = 3 series de 12-15 con menos descanso.
- Los alimentos básicos se cargan solos; el usuario crea recetas con ingredientes.
- Hoy los estilos están en línea en cada FXML (`style="..."`) y no hay hoja de estilos compartida.
- Terminología del producto: "Modo Nutrición", "Modo Gimnasio", "requerimiento diario", "plan semanal", "lista del súper", "registrar consumo", "rutina".
- Idioma de la interfaz: español.
- Sin decidir: si se mantienen los colores actuales (verde para Nutrición y naranja para Gimnasio) y los emojis de los botones. No son compromisos de marca.

## Brand Commitments

- Nombre: **Solea** (confirmado, no se cambia).
- Sin logotipo ni guía de marca en el repositorio.

## Evidence on Hand

- README con el flujo de uso y el modelo de datos: `README.md`, `api/README.md`.
- Plantillas de reportes: `src/main/resources/ni/edu/uam/proyectofinal/reports/`.
- No hay testimonios, usuarios reales, métricas ni capturas. No se deben inventar.

## Product Principles

1. **El siguiente paso siempre a mano.** Cada pantalla deja claro qué viene después en el flujo y lleva los datos ya calculados.
2. **Números en los que se pueda confiar.** Calorías, macros, series y repeticiones se muestran con claridad y en relación con la meta.
3. **Uso diario, no ocasional.** Registrar consumo y revisar el plan tiene que ser rápido.
4. **Dos modos, un producto.** Nutrición y Gimnasio comparten estructura y lenguaje y solo se diferencian donde el objetivo lo exige.
