# SonarQube · S5-inicial

## Backend (API REST) — `pixelmeet-api`

Quality Gate: **ERROR** · Líneas: 2660 · Cobertura: 88.8% · Duplicación: 0.0%

| Métrica | Valor |
|---|---|
| vulnerabilities | 0 |
| bugs | 1 |
| code_smells | 0 |
| security_hotspots | 0 |

### Issues abiertos

| Severidad | Tipo | Regla | Archivo:línea | Mensaje |
|---|---|---|---|---|
| MEDIUM | BUG | java:S6218 | src/main/java/com/duoc/pixelmeet/api/service/ComunidadService.java:87 | Override equals, hashCode and toString to consider array's content in the method |

### Security Hotspots

| Prioridad | Estado | Regla | Archivo:línea | Mensaje |
|---|---|---|---|---|

## Frontend (Thymeleaf) — `pixelmeet-web`

Quality Gate: **ERROR** · Líneas: 2275 · Cobertura: 75.6% · Duplicación: 0.0%

| Métrica | Valor |
|---|---|
| vulnerabilities | 0 |
| bugs | 1 |
| code_smells | 1 |
| security_hotspots | 0 |

### Issues abiertos

| Severidad | Tipo | Regla | Archivo:línea | Mensaje |
|---|---|---|---|---|
| MEDIUM | BUG | java:S6218 | src/main/java/com/duoc/pixelmeet/web/client/ApiDtos.java:123 | Override equals, hashCode and toString to consider array's content in the method |
| MEDIUM | CODE_SMELL | java:S8786 | src/main/java/com/duoc/pixelmeet/web/controller/ComunidadController.java:154 | Simplify this regular expression to reduce its runtime, as it has super-linear performance due to backtracking. |

### Security Hotspots

| Prioridad | Estado | Regla | Archivo:línea | Mensaje |
|---|---|---|---|---|
