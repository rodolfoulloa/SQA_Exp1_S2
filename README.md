# PixelMeet · SQA_Exp1_S2

Actividad sumativa **"Investigando y evaluando la seguridad en aplicaciones web"**
(ISY2202 · Seguridad y Calidad en el Desarrollo de Software · Duoc UC · Experiencia 2, Semana 5).

PixelMeet reúne los eventos de la comunidad gamer. En la semana 1 era una sola aplicación Thymeleaf
con datos en memoria; en las semanas 2 y 3 se separó en frontend y API con JWT, roles, MySQL, HTTPS
y contenedores. En la **semana 4** se agregó la publicación de eventos y se analizó el código con
**SonarQube Community** (ver [Análisis estático con SonarQube](#análisis-estático-con-sonarqube)).
En la **semana 5** se agregan, como funcionalidades privadas, **fotos y videos** en los eventos,
**compartir** en el sitio y en redes sociales, y **comentarios y valoraciones**, y se evalúa la seguridad con
**OWASP Dependency-Check** (herramienta seleccionada), SonarQube, OWASP ZAP y Greenbone OpenVAS
(ver [Evaluación de seguridad (semana 5)](#evaluación-de-seguridad-semana-5)). Informe:
[`docs/Exp2_S5_Grupo20_informe.pdf`](docs/Exp2_S5_Grupo20_informe.pdf).

## Arquitectura

```
Navegador ──HTTPS (443)──▶ frontend (Spring Boot + Thymeleaf + Spring Security)
   │  http (80) ──302──▶ https          │  JWT en header Authorization: Bearer
   │                                    ▼
   │                         backend / API REST (Spring Boot + Spring Security + JWT)
   │                                    │  Spring Data JPA
   │                                    ▼
   │                               MySQL 8.4
```

| Carpeta     | Aplicación      | Responsabilidad |
|-------------|-----------------|-----------------|
| `backend/`  | `pixelmeet-api` | API REST: login con JWT, protección de URLs, datos desde MySQL (JPA) |
| `frontend/` | `pixelmeet-web` | Páginas Thymeleaf; autentica contra la API y guarda el JWT en la sesión del servidor |

En Docker, el frontend publica los puertos 80 y 443 y la API el **8051**, este último para pruebas
directas. MySQL no publica puertos y su red está marcada como `internal`, sin salida a internet.

## Stack

- Java 21 · Spring Boot 4.1 (Spring Framework 7, Spring Security 7)
- Backend: Spring Web MVC, Spring Security, Spring Data JPA, MySQL Driver, Bean Validation, jjwt 0.13
- Frontend: Spring Web MVC, Spring Security, Thymeleaf + extras Spring Security 6, Bootstrap 5 (WebJar)
- MySQL 8.4 · Docker / Docker Compose
- Pruebas: JUnit 5, MockMvc, spring-security-test, H2 (modo MySQL) para las pruebas del backend

## Funcionalidades y acceso

| Funcionalidad | Frontend (`/eventos/...`) | API | Acceso |
|---|---|---|---|
| Página de inicio: eventos recientes y populares, banners comerciales y acceso al login | `/`, `/home` | `GET /api/home` | **Pública** |
| Búsqueda por nombre, videojuego, tipo, país, ciudad, fechas y modalidad (resumen) | `/eventos` | `GET /api/eventos`, `GET /api/eventos/filtros` | **Pública** |
| Detalle: descripción, videojuego, fecha y hora, ubicación o enlace, organizador, requisitos, inscripción y fotografías | `/eventos/{id}` | `GET /api/eventos/{id}` | **Privada** (JWT) |
| **Publicar evento** (el organizador es el usuario autenticado) | `/eventos/nuevo` | `POST /api/eventos` | **Privada** (JWT) |
| **Fotos y videos del evento**: subir fotos JPG/PNG (máx. 5 MB) y agregar enlaces de YouTube o Vimeo. Solo quien publicó el evento o un ADMIN | `/eventos/{id}` → `POST …/fotos`, `…/videos`, `…/media/{m}/eliminar` | `POST /api/eventos/{id}/fotos` (multipart), `POST …/videos`, `DELETE …/media/{m}`, `GET …/media/{m}/contenido` | **Privada** (JWT) |
| **Compartir** en el muro "Compartidos por la comunidad" del inicio o en WhatsApp, X, Facebook y LinkedIn | `POST /eventos/{id}/compartir` | `POST /api/eventos/{id}/compartir` | **Privada** (JWT) |
| **Comentar** (el autor o un ADMIN pueden borrar) y **valorar** de 1 a 5 estrellas (un voto por usuario) | `POST /eventos/{id}/comentarios`, `…/valoracion` | `POST /api/eventos/{id}/comentarios`, `DELETE …/comentarios/{c}`, `PUT …/valoracion`, `GET …/comunidad` | **Privada** (JWT) |
| Token CSRF para las peticiones que modifican datos | — | `GET /api/auth/csrf` | Pública |
| Login | `/login` | `POST /api/auth/login` → JWT | Pública |
| Registro de cuenta (rol USER) | `/registro` | `POST /api/auth/registro` | Pública |
| Perfil del usuario | `/perfil` | `GET/PUT /api/perfil` | Privada (JWT) |
| Datos del usuario del token | — | `GET /api/auth/me` | Privada (JWT) |
| Listado de usuarios | `/admin/usuarios` | `GET /api/admin/usuarios` | Solo **ADMIN** |

Cualquier otra ruta de la API responde 401 sin token o 403 con token (`denyAll`).

## Usuarios de prueba

Se crean en la base de datos al primer arranque, con contraseñas en BCrypt (costo 12).

| Usuario | Correo | Contraseña | Roles |
|---|---|---|---|
| admin  | admin@pixelmeet.cl  | Admin123! | ADMIN, USER |
| gamer1 | gamer1@pixelmeet.cl | Gamer123! | USER |
| gamer2 | gamer2@pixelmeet.cl | Gamer123! | USER |

Se puede iniciar sesión con el usuario o con el correo. Las contraseñas iniciales se cambian con
`SEED_ADMIN_PASSWORD` y `SEED_GAMER_PASSWORD` en `.env`, antes del primer arranque.

## Despliegue en la máquina virtual (Docker)

El `docker-compose.yml` incluye el servidor **MySQL 8.4**: no hay que instalar ni configurar una base de
datos aparte. Al primer arranque se crea la base `pixelmeet`, las tablas (JPA) y los datos iniciales.

Requisitos: Docker Engine con el plugin Compose, y los puertos 80 y 443 abiertos en el firewall o en
el grupo de seguridad de la VM. Abrir también el 8051 si se quiere probar la API desde fuera.

```bash
git clone https://github.com/rodolfoulloa/SQA_Exp1_S2.git
cd SQA_Exp1_S2
docker compose up -d --build
docker compose ps                 # mysql y backend "healthy", frontend "running"
```

Abrir **`http://[IP]/eventos`**: el sitio redirige a `https://[IP]/eventos` y carga la página de inicio.
La búsqueda queda en `/eventos/eventos` y el detalle en `/eventos/eventos/{id}`.

Las rutas fuera del sitio redirigen a él: `http://[IP]/` lleva a `http://[IP]/eventos/`, y
`/home` lleva a `/eventos/home`.
El enunciado también nombra la ruta `/recetas` (texto de otra plantilla del curso), así que funciona
como alias: `http://[IP]/recetas` lleva a `/eventos/` y `/recetas/login` a `/eventos/login`.

### Probar la API directamente (puerto 8051)

```bash
# Rutas públicas
curl http://[IP]:8051/api/home
curl "http://[IP]:8051/api/eventos?modalidad=ONLINE&pais=Chile"

# Token CSRF (cookie XSRF-TOKEN + valor en el cuerpo); se exige en todo POST/PUT/DELETE
curl -c cookies.txt http://[IP]:8051/api/auth/csrf
#   -> {"header":"X-XSRF-TOKEN","token":"<CSRF>"}

# Login: devuelve el JWT (sin el token CSRF responde 403)
curl -b cookies.txt -X POST http://[IP]:8051/api/auth/login -H "Content-Type: application/json" \
     -H "X-XSRF-TOKEN: <CSRF>" -d '{"username":"gamer1","password":"Gamer123!"}'

# Ruta privada con el token
curl http://[IP]:8051/api/eventos/1 -H "Authorization: Bearer <TOKEN>"

# Ruta solo ADMIN: 403 con gamer1, 200 con admin
curl http://[IP]:8051/api/admin/usuarios -H "Authorization: Bearer <TOKEN>"
```

En Windows PowerShell conviene usar `curl.exe` o `Invoke-RestMethod`. El puerto se cambia con
`BACKEND_PORT` en `.env`.

### Credenciales de la infraestructura

Vienen definidas por defecto en `docker-compose.yml`, así que no se necesita un `.env`:

| Uso | Usuario | Contraseña |
|---|---|---|
| MySQL: base `pixelmeet` (la usa el backend) | `pixelmeet` | `game2026` |
| MySQL: administrador | `root` | `root2026` |
| Keystore PKCS12 del certificado TLS | — | `cert2026` |

Para cambiarlas, copiar `.env.example` a `.env` y editar los valores **antes del primer arranque**.
MySQL solo aplica las credenciales al crear el volumen; para cambiarlas después hay que ejecutar
`docker compose down -v`, que borra los datos.

MySQL no publica puertos, por lo que solo es accesible desde la red interna de Docker. Para
consultarla: `docker compose exec mysql mysql -upixelmeet -pgame2026 pixelmeet`.

### Certificado y clave JWT (opcional)

- Por defecto, el certificado autofirmado se emite para `localhost`/`127.0.0.1`. Para incluir la IP
  pública de la VM y fijar una clave JWT propia, ejecutar `./scripts/generar-env.sh <IP_PUBLICA>`
  antes de `docker compose up`.
- Si `JWT_SECRET` queda vacío, la API genera una clave aleatoria al iniciar. Es seguro, pero las
  sesiones abiertas expiran cada vez que se reinicia el backend.
- Como el certificado es autofirmado, el navegador muestra una advertencia la primera vez y hay que
  aceptarla. Con un dominio propio se puede usar un certificado de Let's Encrypt convertido a PKCS12
  (alias `pixelmeet`), copiado en el volumen `certs` como `pixelmeet.p12`.

Comandos útiles:

```bash
docker compose logs -f frontend backend   # ver logs
docker compose down                       # detener (los datos quedan en el volumen mysql-data)
docker compose down -v                    # detener y borrar BD y certificado
```

## Ejecutar en local sin Docker (desarrollo)

Se necesita JDK 21 y un MySQL con la base `pixelmeet`; el contenedor de abajo lo deja listo.

```bash
# MySQL de desarrollo con las mismas credenciales (solo el contenedor de base de datos)
docker run -d --name pixelmeet-mysql -p 3306:3306 -e MYSQL_DATABASE=pixelmeet \
  -e MYSQL_USER=pixelmeet -e MYSQL_PASSWORD=game2026 -e MYSQL_ROOT_PASSWORD=root2026 mysql:8.4

# Backend (puerto 8080); por defecto usa pixelmeet / game2026 en localhost:3306
cd backend
./mvnw spring-boot:run

# Frontend sin HTTPS (puerto 8081): http://localhost:8081/eventos
cd frontend
./mvnw spring-boot:run
```

En Windows se usa `.\mvnw.cmd` y las variables se definen con `$env:VARIABLE="valor"` en PowerShell.

## Pruebas

```bash
cd backend  && ./mvnw clean verify    # 84 pruebas (H2 en memoria, no requiere MySQL)
cd frontend && ./mvnw clean verify    # 70 pruebas (API simulada)
```

Qué cubren:

- **Autenticación:** login correcto con los 3 usuarios, por usuario y por correo; credenciales
  inválidas con un mensaje genérico; bloqueo tras 5 intentos fallidos (429); registro, duplicados
  y claves débiles; intento de escalar a rol ADMIN desde el registro.
- **JWT:** rechazo de tokens sin firma, alterados, firmados con otra clave, vencidos o de otro emisor.
- **Autorización:** rutas públicas sin token; rutas privadas con 401; `/api/admin/**` con 403 para USER.
- **Fotos y videos:** solo el dueño del evento o un ADMIN; archivos que no son imagen aunque digan
  `image/png`, imágenes de más de 4000 px, media de otro evento (IDOR), enlaces de video fuera de la
  lista blanca (`javascript:`, `http://`, dominios parecidos).
- **Comentarios, valoraciones y compartir:** texto vacío o de más de 500 caracteres, borrar comentarios
  ajenos (403), un voto por usuario con promedio correcto, canal no permitido, contenido escapado en
  las vistas (XSS) y enlaces a redes sociales sin redirección abierta.
- **Publicar evento:** 401 sin token, organizador tomado del token, validaciones (fecha futura,
  ubicación o enlace según modalidad, enlaces solo `https://`), CSRF en el formulario.
- **CSRF en la API:** rechazo sin token, con header distinto a la cookie y aceptación con el token
  emitido por `GET /api/auth/csrf`.
- **Búsqueda:** cada filtro y sus combinaciones, rango de fechas inválido, comodines LIKE e
  intentos de inyección SQL.
- **Frontend:** rutas públicas y privadas, login delegado en la API, cierre de sesión cuando el
  token vence, CSRF, escape de contenido (XSS), cabeceras de seguridad y codificación de los
  parámetros enviados a la API.

Además se probó de punta a punta, con MySQL real y el frontend en HTTPS, usando un navegador
automatizado (Playwright). Las capturas están en [`docs/capturas`](docs/capturas).

## Medidas de seguridad implementadas

**Autenticación y autorización (backend)**

- Login `POST /api/auth/login` que valida contra MySQL (BCrypt) y entrega un **JWT HS256** con
  `sub`, `roles`, `iss`, `iat`, `exp` (60 min) y `jti`.
- La clave del JWT llega por la variable de entorno `JWT_SECRET` (mínimo 256 bits) y no está en el
  código. Se valida firma, expiración y emisor.
- En cada petición se recarga el usuario desde la BD, así que una cuenta desactivada deja de tener acceso.
- API **stateless** (sin sesión). **CSRF activo** en toda petición que modifica datos, con el patrón
  *double submit cookie*: `GET /api/auth/csrf` entrega el token en la cookie `XSRF-TOKEN`
  (`HttpOnly`, `Secure`, `SameSite=Strict`) y en el cuerpo; el cliente lo reenvía en la cookie y en
  el header `X-XSRF-TOKEN`. El frontend pide un token nuevo antes de cada POST/PUT.
- Protección de URLs con lista blanca; todo lo no declarado queda en `denyAll`. Rol ADMIN para `/api/admin/**`.
- Respuestas 401 y 403 en JSON, con mensaje genérico para no revelar si el usuario existe.
- Bloqueo temporal del login tras 5 intentos fallidos (fuerza bruta, OWASP A07).

**Datos (OWASP A03 – Inyección)**

- Spring Data JPA y Criteria API: todas las consultas son parametrizadas.
- Se escapan los comodines `%` y `_` en las búsquedas de texto.
- Validación con Bean Validation y listas blancas para avatar, juegos y modalidad.
- Los DTO de entrada no aceptan campos como `roles`, lo que evita la asignación masiva.

**Frontend**

- El JWT se guarda en la **sesión del servidor**. El navegador solo recibe la cookie
  `PIXELMEET_SESSION` con `HttpOnly`, `Secure` y `SameSite=Lax`.
- Si la API responde 401 (token vencido), se invalida la sesión local y se pide login de nuevo.
- CSRF en todos los formularios y cambio de ID de sesión al autenticarse.
- Cabeceras: `Content-Security-Policy` (solo recursos propios), `X-Frame-Options: DENY`,
  `X-Content-Type-Options`, `Referrer-Policy`, `Permissions-Policy` y **HSTS**.
- Salida escapada con `th:text`, sin `th:utext`.
- Los parámetros hacia la API se codifican como variables de plantilla, sin inyección de parámetros.

**HTTPS y contenedores**

- TLS 1.2/1.3 en el frontend. El puerto HTTP solo redirige a HTTPS (302).
- Certificado RSA 3072 / SHA-256 generado al iniciar si no existe, guardado en un volumen con permisos 600.
- Imágenes multi-etapa que se ejecutan con un usuario sin privilegios.
- MySQL no publica puertos y su red es interna. La API se publica en el 8051 solo para pruebas
  (HTTP sin cifrar); en producción se quita esa línea del compose.
- Las credenciales se pueden sobrescribir con `.env` (excluido del repositorio). Las que vienen por
  defecto son simples para facilitar la evaluación; en producción deben reemplazarse.
- Errores sin trazas ni mensajes internos (`server.error.include-stacktrace=never`).

## Análisis estático con SonarQube

Se usa **SonarQube Community** (26.x) en Docker y se analizan por separado el backend
(`pixelmeet-api`) y el frontend (`pixelmeet-web`), incluyendo las plantillas Thymeleaf, el CSS y la
cobertura de pruebas (JaCoCo).

```bash
# 1. Levantar SonarQube (http://localhost:9000, usuario inicial admin/admin: pide cambiarla)
docker run -d --name sonarqube -p 9000:9000 sonarqube:community

# 2. Crear un token (My Account > Security > Global Analysis Token) y guardarlo en .sonar.env
#    (archivo local, no se versiona):
#    SONAR_HOST_URL=http://localhost:9000
#    SONAR_TOKEN=<token>
#    SONAR_ADMIN_USER=admin
#    SONAR_ADMIN_PASSWORD=<clave>

# 3. Analizar ambos proyectos y exportar el resultado a reportes/sonar/<etiqueta>/
scripts/sonar-analizar.sh S4-final
python scripts/sonar-reporte.py S4-final
```

Resultados (detalle en [`reportes/sonar`](reportes/sonar) y en
[`docs/informe-sonarqube-S4.md`](docs/informe-sonarqube-S4.md)):

| Proyecto | Análisis | Vulnerabilidades | Bugs | Code smells | Hotspots | Cobertura | Quality Gate |
|---|---|---|---|---|---|---|---|
| pixelmeet-api | Inicial (S3) | **1 HIGH** | 0 | 2 BLOCKER | 0 | 93,0% | OK |
| pixelmeet-api | Final (S4) | **0** | 0 | 0 | 0 | 92,8% | **OK** |
| pixelmeet-web | Inicial (S3) | 0 | 13 | 1 | 0 | 78,6% | OK |
| pixelmeet-web | Final (S4) | **0** | 0 | 0 | 0 | 81,1% | **OK** |

## Evaluación de seguridad (semana 5)

| Herramienta | Tipo | Cómo se ejecuta | Reportes |
|---|---|---|---|
| **OWASP Dependency-Check 13** (seleccionada) | SCA (dependencias con CVE) | `NVD_API_KEY=<clave> ./mvnw -DskipTests dependency-check:check` en `backend/` y `frontend/`; `python scripts/dc-resumen.py <etiqueta>` | [`reportes/dependency-check`](reportes/dependency-check) |
| SonarQube Community | SAST + calidad | `scripts/sonar-analizar.sh <etiqueta>` y `python scripts/sonar-reporte.py <etiqueta>` | [`reportes/sonar`](reportes/sonar) |
| OWASP ZAP 2.17 | DAST (sitio público y con sesión) | `zap-baseline.py` y el plan [`herramientas/zap/zap-autenticado.yaml`](herramientas/zap/zap-autenticado.yaml) | [`reportes/zap-s5`](reportes/zap-s5) |
| Greenbone OpenVAS (Community) | Escáner de red/TLS | `docker compose -f herramientas/openvas/compose.yaml up -d`; tareas por GMP con [`herramientas/openvas/gmp.sh`](herramientas/openvas/gmp.sh) | [`reportes/openvas`](reportes/openvas) |

La API key de NVD y la clave de OpenVAS se guardan en archivos locales (`.nvd.env`, `.openvas.env`)
que no se versionan.

| Hallazgo | Herramienta | CVSS | Corrección |
|---|---|---|---|
| CVE-2026-60586 / 60623 / 60624 / 61082 en `mysql-connector-j` 9.7.0 | Dependency-Check | 7.7 / 7.1 / 6.5 / 6.5 | `mysql.version` 26.10.0 en `backend/pom.xml` |
| Suites TLS DHE aceptadas: D(HE)ater, CVE-2002-20001 | OpenVAS | 7.5 | `server.ssl.ciphers` solo TLS 1.3 + ECDHE con GCM/ChaCha20 |
| java:S6218 (records con `byte[]`) y java:S8786 (regex con backtracking) | SonarQube | — | `equals`/`hashCode`/`toString` con `Arrays` y `sinBarraFinal` lineal |

Resultado final: Dependency-Check 0 vulnerabilidades (frontend y backend), SonarQube 0 issues y
Quality Gate OK en ambos proyectos, ZAP 0 alertas de riesgo (solo informativas) y OpenVAS 0 altas
(quedan 1 media y 1 baja de la pila TCP del host, ajenas a la aplicación).

El informe está en [`docs/Exp2_S5_Grupo20_informe.pdf`](docs/Exp2_S5_Grupo20_informe.pdf).

## Estructura

```
backend/
  src/main/java/com/duoc/pixelmeet/api
    config/       SecurityConfig, JwtProperties, DataInitializer (datos iniciales)
    security/     JwtService, JwtAuthenticationFilter, UsuarioDetailsService, LoginAttemptService
    model/        Usuario, Rol, Evento, Banner (entidades JPA)
    repository/   Repositorios Spring Data JPA
    service/      AuthService, EventoService, PerfilService, AdminService
    controller/   Auth, Home, Evento, Perfil, Admin
    dto/ exception/
frontend/
  src/main/java/com/duoc/pixelmeet/web
    config/       SecurityConfig, HttpsConfig, ApiProperties
    security/     ApiAuthenticationProvider, ApiUser
    client/       PixelMeetApiClient (RestClient) y DTO
    controller/   Home, Evento, Auth, Perfil, Admin, ApiErrorAdvice
  src/main/resources/templates   Vistas Thymeleaf
  docker/entrypoint.sh            Generación del certificado TLS
docker-compose.yml · .env.example · scripts/ (generar-env, sonar-analizar, sonar-reporte)
herramientas/ (ZAP, OpenVAS) · docs/ (capturas, informes S4 y S5)
reportes/ (sonar, dependency-check, zap-s5, openvas: análisis inicial y final) · sql/
```
