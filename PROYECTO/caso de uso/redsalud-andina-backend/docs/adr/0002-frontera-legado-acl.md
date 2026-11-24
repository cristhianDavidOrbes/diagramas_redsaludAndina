# ADR-002: Capa anticorrupción (ACL) y vista de lectura propia para el legado

- **Estado:** Propuesto (17/11/2026) · **Escenarios:** ASR-3, ASR-7 (restringido por ASR-5)
- **Tensión:** Rendimiento (`p95 <= 2 s`) vs. consistencia inmediata contra SOAP nocturno.

## Contexto
El historial clínico reside en un sistema legado inmutable que expone SOAP nocturno y base de solo lectura. El especialista exige `p95 <= 2 s` con caché y `<= 6 s` en consulta directa, sin filtrar el modelo SOAP al dominio y sin caerse con HTTP `500` cuando el legado no responde.

## Decisión
Aislar SOAP/XML exclusivamente en `legado.infraestructura.salida.soap` tras el puerto de dominio `HistorialClinicoPort`. Implementar:
1. `TraductorLegadoSoap`: traduce `SoapHistorialDto` al modelo canónico `HistorialCanonico` descartando campos administrativos.
2. `AdaptadorSoapResiliente`: `WebServiceTemplate` con timeouts (`<= 5 s`) y `@CircuitBreaker(name = "legadoSoap")`.
3. `CacheTwoLevelAdapter` (`Caffeine L1` con `.recordStats()` + `Redis/ElastiCache L2`) y `GET /historial/{id}` exponiendo la cabecera `X-Data-Freshness` (`live`, `fresh` o `stale`).

## Consecuencias y verificación
- **Positivas:** degradación elegante (`200 OK` con `X-Data-Freshness: stale`, nunca `500`); cambio de proveedor confinado a `legado.infraestructura.salida.soap`.
- **Negativas:** datos servidos desde caché tienen caducidad explícita visible para el especialista.
- **Verificación:** `LegadoAclYFallbackTest` (contrato de traducción + ejercicio detonante con SOAP apagado `< 6 s`).
