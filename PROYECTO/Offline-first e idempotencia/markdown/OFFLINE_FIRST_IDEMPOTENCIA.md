# Offline-First e Idempotencia (Sesión 3)

**Proyecto ProcessFlow:** `redSaludAndina_Offline_Idempotencia` · **ADR asociado:** `ADR-003` · **RF cubiertos:** `RF-09`, `RF-03`

## 1. Modelo de sincronización del borde (5 de 14 centros con 2 Mbps y cortes diarios)
| Garantía exigida | Mecanismo implementado | Criterio verificable |
| --- | --- | --- |
| **No duplicar episodios** | `UUID/ULID` estable generado en cliente + `Idempotency-Key` persistida + `UNIQUE` en Aurora | `POST /sync/episodios` sobrevive reinicio de proceso (nunca `if exists` en memoria) |
| **No perder eventos** | *Transactional Outbox* en misma transacción de Aurora + `SQS FIFO` (`MessageGroupId = agregadoId`) + consumidor idempotente | Sincronización `<= 15 min` tras reconexión y `0` pérdida (verificado con Testcontainers) |
| **Resolver conflictos sin destruir evidencia** | *Last-writer-wins* por campo independiente en lo asistencial + ambos valores en auditoría | Ningún dato asistencial en conflicto se sobrescribe en silencio |
| **Preservar consistencia fuerte** | Rechazo (`409`) y reintento contra servidor para cupos y recetas firmadas | Cero doble reserva y cero modificación de recetas firmadas en el borde |

## 2. Tabla asimétrica de resolución de conflictos (Bloque 3.4)
| Tipo de dato / operación | ¿Se resuelve en el borde? | Política aplicada | Evidencia en auditoría |
| --- | --- | --- | --- |
| Nota de evolución / signos vitales | Sí (campos independientes) | *Last-writer-wins* con sello de tiempo del centro | **Ambos valores** (ganador + desplazado) quedan registrados |
| Adjunto de examen / imagen | Sí (idempotente por ULID) | Anexado único por clave idempotente | Hash, usuario, centro y sello temporal |
| Reserva / cambio de cupo especialista | **No** | **Rechazo** en borde y reintento online contra servidor (`@Version`) | Registro de conflicto de cupo para reprogramación |
| Emisión o cambio de receta firmada | **No** | **Rechazo** en borde y validación/firma online con KMS | Intento rechazado auditado sin emitir borrador |

## 3. Arquitectura Borde + Nube (C4)
```mermaid
flowchart LR
  subgraph Borde___5_Centros_Rurales__2_Mbps_["Borde · 5 Centros Rurales (2 Mbps)<br/><i>Límite de Contenedor</i>"]
    off_pwa("Cliente Offline-First<br/><i>Contenedor</i>")
    off_local_q[("Cola Local Cifrada<br/><i>Base de Datos</i>")]
  end
  subgraph Monolito_Modular___M_dulo_Sincronizaci_n["Monolito Modular · Módulo Sincronización<br/><i>Límite de Contenedor</i>"]
    off_api["POST /sync/episodios<br/><i>Componente</i>"]
    off_conflict["Resolutor de Conflictos<br/><i>Componente</i>"]
    off_outbox["Publicador Outbox<br/><i>Componente</i>"]
    off_cons["Consumidor Idempotente<br/><i>Componente</i>"]
  end
  off_med("Médico Rural<br/><i>Persona</i>")
  off_aurora[("Aurora (UNIQUE + Outbox)<br/><i>Base de Datos</i>")]
  off_sqs("SQS FIFO (Orden por Agregado)<br/><i>Sistema Externo</i>")
  off_aud[("Auditoría Doble Valor (LWW)<br/><i>Base de Datos</i>")]
  off_med -->|"Captura asistencial offline"| off_pwa
  off_pwa -->|"Encola lote (UUID/ULID + ts)"| off_local_q
  off_local_q -->|"Reintento con Idempotency-Key"| off_api
  off_api -->|"Evalúa política de conflicto"| off_conflict
  off_api -->|"Tx atómica K1 + outbox"| off_aurora
  off_outbox -->|"Drena outbox pendiente"| off_aurora
  off_outbox -->|"Publica ordenado por agregado"| off_sqs
  off_sqs -->|"Entrega at-least-once"| off_cons
  off_cons -->|"Audita ambos valores LWW"| off_aud
```

## 4. Modelo Táctico DDD y Event Storming (`Agregado LoteSync`)
```mermaid
flowchart LR
  subgraph Agregado_LoteSync["Agregado LoteSync<br/><i>Agregado</i>"]
    od_root(("LoteSincronización<br/><i>Raíz de Agregado</i>"))
    od_vo(("ClaveIdempotencia<br/><i>Objeto de Valor</i>"))
    od_srv(("ResolutorConflictos<br/><i>Servicio de Dominio</i>"))
  end
  subgraph Salida_Transaccional_y_Auditor_a["Salida Transaccional y Auditoría<br/><i>Contexto Delimitado</i>"]
    od_repo(("RepoOutboxAurora<br/><i>Repositorio</i>"))
    od_reg(("Regla Asimetría<br/><i>Regla de Negocio</i>"))
  end
  od_act("App Móvil Offline<br/><i>Sistema Externo</i>")
  od_cmd1(("SincronizarLote<br/><i>Comando</i>"))
  od_ev1(("LoteAceptadoIdempotente<br/><i>Evento</i>"))
  od_pol(("PolíticaConflicto<br/><i>Política</i>"))
  od_cmd2(("ResolverConflictoLote<br/><i>Comando</i>"))
  od_ev2(("ConflictoAuditado<br/><i>Evento</i>"))
  od_sqs("SQS FIFO Ordenado<br/><i>Sistema Externo</i>")
  od_act -->|"reintenta 3x"| od_cmd1
  od_cmd1 -->|"persiste único"| od_ev1
  od_cmd1 -->|"valida"| od_root
  od_ev1 -->|"evalúa"| od_pol
  od_pol -->|"dispara"| od_cmd2
  od_cmd2 -->|"emite"| od_ev2
  od_cmd2 -->|"publica outbox"| od_repo
  od_root -->|"verifica UNIQUE"| od_vo
  od_root -->|"resuelve"| od_srv
  od_repo -->|"cumple"| od_reg
  od_repo -->|"MessageGroupId"| od_sqs
```

## 5. Secuencia: Triple Envío, Reinicio de Proceso y Conflictos
```mermaid
sequenceDiagram
    autonumber
    participant C as Cliente en centro (UUID/ULID estable)
    participant LB as Balanceador
    participant S as POST /sync/episodios
    participant ID as Tabla Idempotency-Key (UNIQUE)
    participant DB as Aurora PostgreSQL
    participant OB as Outbox (misma tx)
    participant P as Publicador Outbox
    participant Q as SQS FIFO (orden por agregado)
    participant K as Consumidor idempotente
    participant A as Auditoría (ambos valores LWW)

    Note over C: 5 de 14 centros con 2 Mbps y cortes diarios
    C->>LB: Lote L1 (episodio=ULID-42, Idempotency-Key=K1)
    LB->>S: Intento 1
    S->>ID: ¿Existe K1?
    ID-->>S: No
    S->>DB: INSERT episodio (ULID-42)
    DB-->>S: OK (unique constraint)
    S->>OB: Evento en la MISMA transacción
    S->>ID: Persistir K1 + respuesta R
    S-->>C: 201 R
    P->>OB: Drena eventos pendientes
    P->>Q: Publica con MessageGroupId = agregadoId (at-least-once)
    Q->>K: Entrega ordenada por agregado
    K->>K: Deduplica por eventoId
    K->>A: Aplica LWW asistencial + registra ambos valores en conflicto
    Note over C,S: Corte de red — reintento 2 del MISMO lote
    C->>LB: Lote L1 (ULID-42, K1) — intento 2
    LB->>S: Reintento
    S->>ID: ¿Existe K1?
    ID-->>S: Sí, respuesta R
    S-->>C: 200 R (sin nuevo INSERT)
    Note over S,DB: Reinicio del proceso — la memoria no sobrevive
    C->>LB: Lote L1 (ULID-42, K1) — intento 3
    LB->>S: Reintento (otra instancia)
    S->>ID: ¿Existe K1 en BD?
    ID-->>S: Sí, respuesta R
    S-->>C: 200 R (0 duplicados, 0 pérdida)
    Note over C,A: Cupos y recetas firmadas NO se resuelven en el borde
    C->>S: Reserva de cupo / receta firmada offline
    S-->>C: 409 Rechazo + reintento contra servidor (consistencia fuerte)
```
