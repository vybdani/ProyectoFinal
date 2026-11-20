# API REST Solea (Spring Boot)

Microservicio backend desarrollado en **Spring Boot 3.4.2** con **Java 21**, diseñado para apoyar a la aplicación de escritorio en JavaFX.

## ¿Cómo apoya a la aplicación de escritorio?
1. **API Propia:** Proporciona endpoints REST (`/api/consejos`, `/api/calculo/macros`, `/api/status`) para centralizar la lógica nutricional.
2. **Apoyo de API de la Red:** Se conecta a la API abierta mundial de **Open Food Facts** para buscar información nutricional y macronutrientes de alimentos reales en internet (`/api/alimentos/buscar?q=...`).

---

## Cómo ejecutar la API

### Opción 1: Desde IntelliJ IDEA
1. Navega a `api/src/main/java/ni/edu/uam/api/SoleaApiApplication.java`.
2. Haz clic derecho y selecciona **Run 'SoleaApiApplication'** (o presiona Shift + F10).
3. El servidor iniciará en `http://localhost:8080`.

### Opción 2: Desde la terminal (PowerShell o CMD)
```bash
cd api
mvn spring-boot:run
```

---

## Endpoints Disponibles

| Método | Endpoint | Descripción | Ejemplo de prueba en navegador |
|---|---|---|---|
| `GET` | `/api/status` | Comprueba que la API esté activa | `http://localhost:8080/api/status` |
| `GET` | `/api/alimentos/buscar?q={nombre}` | Busca alimentos en internet con Open Food Facts | `http://localhost:8080/api/alimentos/buscar?q=avena` |
| `GET` | `/api/consejos?objetivo={objetivo}` | Recomendaciones según objetivo (`bajar`, `mantener`, `volumen`, `definicion`) | `http://localhost:8080/api/consejos?objetivo=volumen` |
| `POST` | `/api/calculo/macros` | Cálculo instantáneo de macros | Parámetros: `pesoKg`, `alturaCm`, `edad`, `sexo`, `objetivo` |

---

## Cómo cambiar el nombre del proyecto cuando decidan el nombre final
1. En `api/pom.xml`, edita las líneas:
   ```xml
   <artifactId>TU_NUEVO_NOMBRE</artifactId>
   <name>TU_NUEVO_NOMBRE</name>
   ```
2. En `api/src/main/resources/application.properties`, edita:
   ```properties
   spring.application.name=TU_NUEVO_NOMBRE
   ```
3. Si lo deseas, puedes renombrar la carpeta `api/` al nombre de tu preferencia.
