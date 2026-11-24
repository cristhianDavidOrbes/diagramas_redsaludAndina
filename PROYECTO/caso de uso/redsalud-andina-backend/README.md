# RedSalud Andina — Backend Monolito Modular (Spring Boot 3.5 + Java 21)

Implementación concisa, hexagonal y verificada con **ArchUnit** del taller *Backend resiliente para teleconsulta rural: de los datos al código con Spring Boot*, alineada 1:1 con los diagramas C4, BPMN, DDD, ACL Legado, Offline-First e Idempotencia y Seguridad/Auditoría.

## Módulos y esquemas (9 contextos delimitados)

| Subdominio | Módulo (`co.edu.ucc.redsalud.*`) | Esquema Flyway | Responsabilidad clave |
| --- | --- | --- | --- |
| Core | `agenda` | `agenda` | Cupos de especialista con `@Version` (sin doble reserva) |
| Core | `interconsulta` | `interconsulta` | Máquina de estados `SOLICITADA → AGENDADA → ATENDIDA → CERRADA` |
| Core | `episodio` | `episodio` | Episodio clínico + adjuntos S3 cifrados con KMS (región única) |
| Core | `receta` | `receta` | Receta inmutable con firma KMS SHA-256 + despacho SQS FIFO |
| Soporte | `sincronizacion` | `sincronizacion` | `POST /sync/episodios` idempotente, Outbox + SQS FIFO, LWW doble valor / rechazo `409` |
| Soporte | `analitica` | `analitica` | Indicadores de adopción y SLO `>= 99,5 %` con Micrometer |
| Genérico | `identidad` | `identidad` | ACL OIDC/OAuth2 + ABAC (`@PreAuthorize` + `AuthorizationManager`) en servicio |
| Genérico | `legado` | `legado` | ACL SOAP + Resilience4j + caché L1/L2 + `GET /historial/{id}` con `X-Data-Freshness` |
| Genérico | `auditoria` | `auditoria` | Bitácora *append-only* con cadena SHA-256 + S3 Object Lock (`COMPLIANCE`) + reporte |

## Cómo ejecutar

```bash
# 1. Ejecutar todas las pruebas (incluye 32 reglas ArchUnit + JaCoCo)
mvn test

# 2. Levantar PostgreSQL 16 + Redis 7 + LocalStack (S3/SQS/KMS) localmente
docker compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=postgres

# 3. Generar reporte reproducible de auditoría clínica
powershell -ExecutionPolicy Bypass -File scripts/generar-reporte-auditoria.ps1
```

## Documentación y ADRs
- [Mapa contexto → módulo → esquema, traducción ACL y tabla de conflictos](docs/tabla-contextos-esquemas.md)
- [ADR-001: Monolito modular](docs/adr/0001-monolito-modular.md)
- [ADR-002: Frontera con el legado (ACL + CQRS)](docs/adr/0002-frontera-legado-acl.md)
- [ADR-003: Offline-first e idempotencia](docs/adr/0003-offline-first-idempotencia.md)
- [ADR-004: Residencia, ABAC y auditoría inalterable](docs/adr/0004-residencia-y-auditoria.md)
- [Reporte reproducible de auditoría clínica](docs/reporte-auditoria-ejemplo.md)
