# Informe · Análisis de código estático con SonarQube

ISY2202 · Seguridad y Calidad en el Desarrollo de Software · Experiencia 2, Semana 4
Actividad: *Configurando y ejecutando la herramienta de análisis de código estático* · Proyecto PixelMeet

## 1. Objetivo

Instalar y configurar SonarQube Community Edition, analizar por separado el frontend y el backend
de PixelMeet, corregir todas las vulnerabilidades críticas y demás hallazgos, y verificar con un
análisis final que la aplicación queda sin vulnerabilidades. En la misma entrega se agrega la
funcionalidad privada **Publicar evento**.

## 2. Instalación y configuración de SonarQube

| Elemento | Valor |
|---|---|
| Edición | SonarQube Community Build 26.11 (imagen oficial `sonarqube:community`) |
| Ejecución | Docker, puerto 9000, volúmenes para datos, extensiones y logs |
| Proyectos | `pixelmeet-api` (backend) y `pixelmeet-web` (frontend) |
| Autenticación del escáner | Token de tipo *Global Analysis Token* (no se versiona; queda en `.sonar.env`) |
| Escáner | `sonar-maven-plugin` 5.1 ejecutado con el wrapper Maven de cada módulo |
| Cobertura | JaCoCo 0.8.13 (`prepare-agent` + `report`) durante `mvn verify` |
| Quality Gate | *Sonar way* (por defecto): 0 issues nuevos, cobertura de código nuevo ≥ 80%, duplicación ≤ 3%, hotspots revisados |

Pasos:

1. `docker run -d --name sonarqube -p 9000:9000 sonarqube:community`
2. Ingresar a `http://localhost:9000` con `admin/admin` y cambiar la contraseña.
3. Crear los proyectos `pixelmeet-api` y `pixelmeet-web` (creación manual).
4. Generar el token de análisis y guardarlo en `.sonar.env`.
5. Ejecutar `scripts/sonar-analizar.sh <etiqueta>`, que analiza ambos módulos e incluye como fuentes
   `src/main/java`, `src/main/resources` (plantillas Thymeleaf, CSS y configuración) y `pom.xml`.
6. Exportar los resultados con `python scripts/sonar-reporte.py <etiqueta>`. La Community Edition
   no genera PDF, por lo que se usa su API web (`/api/issues/search`, `/api/hotspots/search`,
   `/api/measures/component`, `/api/qualitygates/project_status`).

## 3. Análisis inicial (código de la semana 3)

Etiqueta `S3-inicial`. Detalle completo en [`reportes/sonar/S3-inicial`](../reportes/sonar/S3-inicial).

| Proyecto | Vulnerabilidades | Bugs | Code smells | Hotspots | Cobertura |
|---|---|---|---|---|---|
| pixelmeet-api | **1 (HIGH)** | 0 | 2 (BLOCKER) | 0 | 93,0% |
| pixelmeet-web | 0 | 13 (MEDIUM) | 1 (MEDIUM) | 0 | 78,6% |

Hallazgos:

| # | Proyecto | Severidad | Tipo | Regla | Ubicación | Descripción |
|---|---|---|---|---|---|---|
| 1 | api | **HIGH** | Vulnerabilidad | `java:S4502` | `config/SecurityConfig.java` | Protección CSRF de Spring Security desactivada |
| 2 | api | BLOCKER | Code smell | `java:S2699` | `AuthApiTest.java:31` | Prueba sin aserciones |
| 3 | api | BLOCKER | Code smell | `java:S2699` | `AuthApiTest.java:38` | Prueba sin aserciones |
| 4 | web | MEDIUM | Bug | `Web:PageWithoutTitleCheck` | 8 plantillas | Página sin etiqueta `<title>` |
| 5 | web | MEDIUM | Bug | `Web:InputWithoutLabelCheck` | `perfil.html` (5) | Campo sin `id` asociado a un `<label>` |
| 6 | web | MEDIUM | Code smell | `Web:S9379` | `login.html:34` | Atributo `autofocus` (accesibilidad) |

## 4. Correcciones

### 4.1 `java:S4502` · CSRF desactivado en la API (vulnerabilidad crítica)

**Causa.** La API es stateless y recibe el JWT en el header `Authorization`, por lo que en la
semana 2 se desactivó CSRF (`csrf(AbstractHttpConfigurer::disable)`). SonarQube marca cualquier
desactivación o excepción de CSRF como vulnerabilidad: si en el futuro la API aceptara cookies,
quedaría expuesta a peticiones forjadas.

**Corrección.** Se activó la protección CSRF de Spring Security en toda petición que modifica datos
(POST, PUT, PATCH, DELETE), con el patrón *double submit cookie*, sin sesión en el servidor:

- `CookieCsrfTokenRepository`: el token vive en la cookie `XSRF-TOKEN` (`HttpOnly`, `Secure`,
  `SameSite=Strict`), no en la sesión, y la API sigue siendo stateless.
- Nuevo endpoint público `GET /api/auth/csrf` que emite el token (cookie + valor en el cuerpo y
  nombre del header).
- El cliente debe reenviar el token en la cookie **y** en el header `X-XSRF-TOKEN`; si falta o no
  coincide, la API responde 403. Un sitio externo no puede leer la cookie ni agregar el header.
- El frontend (`PixelMeetApiClient.conCsrf`) pide un token nuevo antes de cada POST/PUT (login,
  registro, perfil y publicar evento), sin compartir estado entre usuarios.

Pruebas agregadas (`CsrfApiTest`): POST/PUT sin token → 403; header distinto a la cookie → 403;
token emitido por la API → 200; la cookie es `HttpOnly` y `Secure`.

### 4.2 `java:S2699` · Pruebas sin aserciones

Las pruebas `losTresUsuariosInicialesPuedenIniciarSesion` y `sePuedeIniciarSesionConElCorreo`
solo llamaban al login. Se agregó una aserción explícita sobre el JWT devuelto
(`assertThat(login(...)).isNotBlank()`).

### 4.3 `Web:PageWithoutTitleCheck` · Páginas sin `<title>`

El título se generaba dentro del fragmento `head`, que reemplaza la cabecera completa, por lo que
el analizador no lo veía en cada vista. Se aplicó el patrón de *layout* de Thymeleaf: cada página
declara su propio `<title>` y lo entrega al fragmento con `~{::title}`
(`<head th:replace="~{fragments :: head(~{::title})}"><title>Mi perfil | PixelMeet</title></head>`).
El título que recibe el navegador es el mismo de antes.

### 4.4 `Web:InputWithoutLabelCheck` · Campos sin etiqueta asociada

En `perfil.html`, los radio de avatar y los checkbox de juegos quedan dentro de su `<label>`
(asociación implícita), y los checkbox de notificaciones tienen `id` fijo con `<label for>`.
Mejora la accesibilidad con lectores de pantalla.

### 4.5 `Web:S9379` · `autofocus` en el login

Se quitó el atributo `autofocus` del campo de usuario.

## 5. Funcionalidad nueva: Publicar evento [Privada]

El caso pide "publicar recetas", texto que corresponde a otra plantilla del curso. En el caso
PixelMeet (comunidad gamer) la funcionalidad equivalente es **publicar eventos**, igual que la ruta
pública del sitio es `/eventos`.

| Capa | Implementación | Medidas de seguridad |
|---|---|---|
| API | `POST /api/eventos` → 201 con el detalle | Requiere JWT (USER o ADMIN) y token CSRF; Bean Validation en todos los campos (largos, solo letras en país y ciudad, enlaces solo `https://`); fecha futura; ubicación obligatoria si es presencial y enlace si es online; el **organizador se toma del token**, no de la petición (evita suplantación y asignación masiva); se guarda con JPA (consultas parametrizadas) |
| Frontend | `/eventos/nuevo` (formulario Thymeleaf) | Ruta privada; formulario con CSRF; validación en servidor; salida escapada con `th:text`; los errores de la API se muestran por campo |

Datos que se piden: nombre, videojuego, tipo, modalidad, fecha y hora, país, ciudad, ubicación o
enlace, resumen, descripción, requisitos, inscripción, precio y cupos.

Sobre el registro: la instrucción lo marca como "[Privada]", pero un usuario sin cuenta no puede
autenticarse antes de crearla. El registro sigue siendo público. Pide nombre completo, correo y
contraseña, y la contraseña se guarda cifrada con BCrypt (costo 12). Lo que sí es privado es todo
lo que requiere la cuenta: detalle de eventos, perfil y publicación de eventos.

## 6. Análisis final

Etiqueta `S4-final`. Incluye la funcionalidad nueva. Detalle en
[`reportes/sonar/S4-final`](../reportes/sonar/S4-final).

| Proyecto | Vulnerabilidades | Bugs | Code smells | Hotspots | Cobertura | Duplicación | Quality Gate |
|---|---|---|---|---|---|---|---|
| pixelmeet-api | **0** | 0 | 0 | 0 | 92,8% | 0,0% | **Passed** |
| pixelmeet-web | **0** | 0 | 0 | 0 | 81,1% | 0,0% | **Passed** |

**La aplicación queda sin vulnerabilidades críticas ni de ningún otro nivel**, sin bugs ni code
smells, y ambos proyectos aprueban el Quality Gate.

## 7. Verificación funcional

- Pruebas automatizadas: 59 en el backend y 52 en el frontend, todas aprobadas (`mvn verify`).
- Prueba de punta a punta sobre Docker (MySQL real, frontend en HTTPS):
  - Inicio de sesión con `gamer1`.
  - Publicación de un evento desde `/eventos/nuevo`: redirige al detalle con el mensaje
    "Evento publicado correctamente" y el usuario como organizador.
  - El formulario sin token CSRF responde 403.
  - Un usuario anónimo es redirigido al login.
  - La API rechaza el login sin token CSRF con 403.

## 8. Evidencias

- `reportes/sonar/S3-inicial/` y `reportes/sonar/S4-final/`: `resumen.md` y JSON completos por
  proyecto (issues, hotspots, métricas y Quality Gate).
- Capturas del panel de SonarQube (`docs/capturas/`):

| Archivo | Contenido |
|---|---|
| `sonar-01-proyectos.png` | Ambos proyectos con Quality Gate aprobado y calificación A en seguridad, confiabilidad y mantenibilidad |
| `sonar-02-api-resumen.png` | Resumen de `pixelmeet-api`: Passed, 0 issues, cobertura 92,8%, duplicación 0,0% |
| `sonar-03-api-issues-abiertos.png` | `pixelmeet-api` sin issues abiertos |
| `sonar-04-api-issues-corregidos.png` | Issues corregidos de `pixelmeet-api` (CSRF `java:S4502` y pruebas sin aserciones). La regla CSRF aparece 2 veces: el análisis intermedio, que solo exigía CSRF con cookies, también la marcó |
| `sonar-05-api-actividad.png` | Historial: análisis S3-inicial y S4-final con la evolución de issues |
| `sonar-06-web-resumen.png` | Resumen de `pixelmeet-web`: Passed, 0 issues, cobertura 81,1% |
| `sonar-07-web-issues-abiertos.png` | `pixelmeet-web` sin issues abiertos |
| `sonar-08-web-issues-corregidos.png` | 14 issues corregidos de `pixelmeet-web` (títulos, labels, autofocus) |
| `sonar-09-web-actividad.png` | Historial de `pixelmeet-web`: de 14 issues a 0 |
