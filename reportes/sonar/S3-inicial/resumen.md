# SonarQube · S3-inicial

## Backend (API REST) — `pixelmeet-api`

Quality Gate: **OK** · Líneas: 1654 · Cobertura: 93.0% · Duplicación: 0.0%

| Métrica | Valor |
|---|---|
| vulnerabilities | 1 |
| bugs | 0 |
| code_smells | 2 |
| security_hotspots | 0 |

### Issues abiertos

| Severidad | Tipo | Regla | Archivo:línea | Mensaje |
|---|---|---|---|---|
| BLOCKER | CODE_SMELL | java:S2699 | src/test/java/com/duoc/pixelmeet/api/AuthApiTest.java:31 | Add at least one assertion to this test case. |
| BLOCKER | CODE_SMELL | java:S2699 | src/test/java/com/duoc/pixelmeet/api/AuthApiTest.java:38 | Add at least one assertion to this test case. |
| HIGH | VULNERABILITY | java:S4502 | src/main/java/com/duoc/pixelmeet/api/config/SecurityConfig.java:44 | Make sure disabling Spring Security's CSRF protection is safe here. |

### Security Hotspots

| Prioridad | Estado | Regla | Archivo:línea | Mensaje |
|---|---|---|---|---|

## Frontend (Thymeleaf) — `pixelmeet-web`

Quality Gate: **OK** · Líneas: 1508 · Cobertura: 78.6% · Duplicación: 0.0%

| Métrica | Valor |
|---|---|
| vulnerabilities | 0 |
| bugs | 13 |
| code_smells | 1 |
| security_hotspots | 0 |

### Issues abiertos

| Severidad | Tipo | Regla | Archivo:línea | Mensaje |
|---|---|---|---|---|
| MEDIUM | BUG | Web:PageWithoutTitleCheck | src/main/resources/templates/admin-usuarios.html:3 | Add a <title> tag to this page. |
| MEDIUM | BUG | Web:PageWithoutTitleCheck | src/main/resources/templates/error.html:3 | Add a <title> tag to this page. |
| MEDIUM | BUG | Web:PageWithoutTitleCheck | src/main/resources/templates/evento-detalle.html:3 | Add a <title> tag to this page. |
| MEDIUM | BUG | Web:PageWithoutTitleCheck | src/main/resources/templates/eventos.html:3 | Add a <title> tag to this page. |
| MEDIUM | BUG | Web:PageWithoutTitleCheck | src/main/resources/templates/home.html:4 | Add a <title> tag to this page. |
| MEDIUM | BUG | Web:PageWithoutTitleCheck | src/main/resources/templates/login.html:3 | Add a <title> tag to this page. |
| MEDIUM | CODE_SMELL | Web:S9379 | src/main/resources/templates/login.html:34 | Remove this "autofocus" attribute, as it can reduce usability and accessibility for users. |
| MEDIUM | BUG | Web:PageWithoutTitleCheck | src/main/resources/templates/perfil.html:3 | Add a <title> tag to this page. |
| MEDIUM | BUG | Web:InputWithoutLabelCheck | src/main/resources/templates/perfil.html:43 | Add an "id" attribute to this input field and associate it with a label. |
| MEDIUM | BUG | Web:InputWithoutLabelCheck | src/main/resources/templates/perfil.html:54 | Add an "id" attribute to this input field and associate it with a label. |
| MEDIUM | BUG | Web:InputWithoutLabelCheck | src/main/resources/templates/perfil.html:63 | Add an "id" attribute to this input field and associate it with a label. |
| MEDIUM | BUG | Web:InputWithoutLabelCheck | src/main/resources/templates/perfil.html:67 | Add an "id" attribute to this input field and associate it with a label. |
| MEDIUM | BUG | Web:InputWithoutLabelCheck | src/main/resources/templates/perfil.html:71 | Add an "id" attribute to this input field and associate it with a label. |
| MEDIUM | BUG | Web:PageWithoutTitleCheck | src/main/resources/templates/registro.html:3 | Add a <title> tag to this page. |

### Security Hotspots

| Prioridad | Estado | Regla | Archivo:línea | Mensaje |
|---|---|---|---|---|
