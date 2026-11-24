# ADR-003: Cliente offline-first, API idempotente y Transactional Outbox

- **Estado:** Propuesto (17/11/2026) · **Escenarios:** ASR-1 (restringido por ASR-5)
- **Tensión:** Disponibilidad rural (5 de 14 centros con 2 Mbps y cortes diarios) vs. consistencia fuerte.

## Contexto
En centros rurales con cortes diarios, los médicos deben registrar evolución, signos vitales y adjuntos sin pérdida de datos y sincronizar en `<= 15 min` al reconectar, sobreviviendo reintentos múltiples y reinicios del servidor.

## Decisión
Implementar en el módulo `sincronizacion`:
1. `POST /sync/episodios` con cabecera `Idempotency-Key` (UUID/ULID generado en cliente) persistida con restricción `UNIQUE` en `sincronizacion.claves_idempotencia`. Los reintentos devuelven `200 OK` sin nuevo `INSERT`.
2. *Transactional Outbox* (`sincronizacion.outbox_eventos`) en la misma transacción + `SqsFifoOutboxAdapter` con `MessageGroupId = agregadoId` para mantener orden estricto por agregado.
3. **Política asimétrica de conflictos (`ResolutorConflictos`)**: *last-writer-wins* por campo independiente en lo asistencial registrando **ambos valores** (`valorGanador` y `valorDesplazado`) en `sincronizacion.conflictos_lww`; **rechazo `409`** para `RESERVA_CUPO` y `RECETA_FIRMADA` exigiendo consistencia fuerte en servidor.

## Verificación
`SincronizacionIdempotenciaTest` (triple envío con orden alterado, simulación de reinicio de servicio sobre almacén persistente, LWW doble valor y rechazo `409`) y `PersistenciaPostgresTestcontainersTest` (PostgreSQL real con Flyway).
