# ADR-004: Región única, autorización ABAC y bitácora de auditoría inalterable

- **Estado:** Propuesto (17/11/2026) · **Escenarios:** ASR-5, ASR-6 (restringido por ASR-2 y ASR-3)
- **Tensión:** Seguridad y cumplimiento normativo vs. latencia y complejidad operativa.

## Contexto
Los datos clínicos no pueden salir del país (región AWS única sin réplicas externas), solo personal clínico con relación vigente puede consultar historias y el 100 % de lecturas —incluidas las denegaciones— debe quedar en bitácora demostrable e inalterable.

## Decisión
1. **Residencia y KMS (`episodio`):** `AdjuntoCifrado` exige `kmsKeyId`, valida `regionPermitida` y rechaza `replicacionExterna = true`. `S3AdjuntoKmsAdapter` cifra con `AWS_KMS` y expone política de bucket negando `s3:PutBucketReplication` y cargas sin KMS.
2. **Autorización ABAC en servicio (`identidad`):** `AutorizadorAbac` valida rol clínico, estado activo, motivo y relación por centro/paciente. Se aplica en capa de servicio/configuración mediante `@PreAuthorize` (`PoliticaAbacGate`) y `AuthorizationManager` en `IdentidadConfig` (nunca en controladores), registrando el 100 % de denegaciones. `TraductorJwtOidc` aísla los claims OAuth2.
3. **Auditoría inalterable (`auditoria`):** `TrazaAccesoClinico` encadena huellas `SHA-256` (`hashAnterior -> hashActual`), persiste en transacción `REQUIRES_NEW` y archiva en `S3ObjectLockWormAdapter` (`ObjectLockMode.COMPLIANCE`). `AuditarAccesoUseCase.generarReporte` genera el reporte reproducible *"quién vio este documento y con qué motivo"*.

## Verificación
`SeguridadAuditoriaYResidenciaTest` (test negativo `403` + auditoría de denegación, política KMS/región única, detección de alteración en cadena SHA-256 y reporte reproducible).
