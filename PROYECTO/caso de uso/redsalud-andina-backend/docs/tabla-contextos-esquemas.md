# Mapa Contexto → Módulo → Esquema (RedSalud Andina)

## 1. Propiedad del dato por contexto delimitado (Bloque 1.1)

| Subdominio | Contexto delimitado | Módulo Spring Boot (`co.edu.ucc.redsalud.*`) | Esquema PostgreSQL / Aurora | Consistencia | Regla de frontera |
| --- | --- | --- | --- | --- | --- |
| Core · Atención Teleclínica | Interconsulta | `interconsulta` | `interconsulta` | Fuerte (`@Version`) | Máquina de estados `SOLICITADA → AGENDADA → ATENDIDA → CERRADA` |
| Core · Atención Teleclínica | Agenda | `agenda` | `agenda` | Fuerte (`@Version`) | Sin doble reserva; prohibido `JOIN` con `episodio` |
| Core · Atención Teleclínica | Episodio Clínico | `episodio` | `episodio` | Fuerte + S3 KMS | Adjuntos cifrados KMS sin replicación cross-region |
| Core · Atención Teleclínica | Receta Electrónica | `receta` | `receta` | Fuerte + SQS FIFO | Inmutable tras firma KMS; despacho FIFO por `recetaId` |
| Soporte · Sync y Analítica | Sincronización Offline | `sincronizacion` | `sincronizacion` | Eventual ordenada + Outbox | `Idempotency-Key UNIQUE` + LWW doble valor / rechazo `409` |
| Soporte · Sync y Analítica | Analítica de Uso | `analitica` | `analitica` | Eventual (SLO 99,5 %) | Proyección Micrometer sin contaminar OLTP |
| Genérico · Acceso, Legado y Aud. | Identidad y Acceso | `identidad` | `identidad` | Fuerte (ABAC) | ACL OIDC/OAuth2 + `@PreAuthorize` + `AuthorizationManager` en servicio |
| Genérico · Acceso, Legado y Aud. | Integración Legado | `legado` | `legado` | Lectura eventual con TTL | ACL SOAP + Resilience4j + Caffeine L1 / Redis L2 + `X-Data-Freshness` |
| Genérico · Acceso, Legado y Aud. | Auditoría | `auditoria` | `auditoria` | Append-only + WORM | Cadena SHA-256 + S3 Object Lock `COMPLIANCE` (`REQUIRES_NEW`) |

## 2. Mapa de traducción ACL Legado (Bloque 2.5)

| Campo Legado SOAP | Campo Canónico (`HistorialCanonico`) | Regla en `TraductorLegadoSoap` |
| --- | --- | --- |
| `COD_PAC_HIST` (`LEG-<id>`) | `pacienteId` | Normaliza prefijo legado; nunca entra al dominio |
| `XML_RESUMEN_ANT` | `resumenAntecedentes` | Minimizado y recortado; sin campos administrativos |
| `ADMIN_CODE` | *(descartado)* | Bloqueado en la frontera ACL |
| `FEC_ULT_CARGA_NOCT` | `fechaSincronizacion` | Expuesto en cabecera HTTP `X-Data-Freshness` |

## 3. Tabla asimétrica de resolución de conflictos offline (Bloque 3.4)

| Operación offline | ¿Resuelve en borde? | Política | Evidencia en auditoría |
| --- | --- | --- | --- |
| `NOTA_EVOLUCION` / `SIGNOS_VITALES` | Sí (por campo) | *Last-writer-wins* por sello temporal | `ConflictoLww` guarda **valor ganador y valor desplazado** |
| `ADJUNTO_EXAMEN` | Sí (idempotente) | Deduplicado por `Idempotency-Key` | Clave persistida + outbox |
| `RESERVA_CUPO` | **No** | **Rechazo `409`** y reintento online | Resultado `RECHAZADO_BORDE` persistido |
| `RECETA_FIRMADA` | **No** | **Rechazo `409`** y firma KMS online | Resultado `RECHAZADO_BORDE` persistido |
