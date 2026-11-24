# RedSalud Andina — Arquitectura C4 y Vistas Diferenciadas

**Proyecto:** `RedSalud Andina - Arquitectura` · **Stack:** Java 21 (LTS), Spring Boot 3.x, React/PWA, AWS (Región única)
**Postura arquitectónica:** Monolito modular con contextos delimitados (límites verificables con `ArchUnit` / `Spring Modulith`), extrayendo únicamente señalización de video gestionada (`WebRTC`) e ingesta/archivado inmutable de auditoría (`S3 Object Lock`).

## 1. Decisiones arquitectónicas cerradas (ADRs)
- **ADR-001 (Monolito modular):** un módulo por contexto delimitado con esquema propio en Aurora PostgreSQL Multi-AZ; DynamoDB únicamente para perfiles de identidad y caché del legado; comunicación interna por eventos (prohibido `@ManyToOne` y `JOIN` entre módulos).
- **ADR-002 (Frontera legado ACL + CQRS):** adaptador anticorrupción frente a SOAP nocturno + BD solo lectura; vista de lectura con caché L1/L2 (`Caffeine` + `ElastiCache/DynamoDB`) y cabecera `X-Data-Freshness` (`p95 <= 2 s` caché / `<= 6 s` directo).
- **ADR-003 (Offline-first e idempotencia):** cliente con UUID/ULID + sello del centro, `Idempotency-Key` persistida con restricción `UNIQUE`, *Transactional Outbox* + `SQS FIFO` ordenado por agregado, conflictos LWW con doble auditoría en lo asistencial y rechazo contra servidor en cupos/recetas firmadas.
- **ADR-004 y ADR-005 (Residencia y auditoría demostrable):** región AWS única sin réplicas ni backups externos, cifrado TLS + KMS con claves por contexto y rotación, autorización ABAC (`@PreAuthorize` + `AuthorizationManager`) en capa de servicio y auditoría *append-only* en `S3 Object Lock (Compliance)` con reporte reproducible.

## 2. Matriz de requisitos de calidad (ASR-1 a ASR-9)
| ID | Atributo | Escenario | Métrica objetivo | Decisión arquitectónica |
| --- | --- | --- | --- | --- |
| ASR-1 | Disponibilidad | Corte de enlace rural (5/14 centros, 2 Mbps) | Continuidad local; `0` pérdida; sync `<= 15 min` | Cliente offline-first + cola local + API idempotente |
| ASR-2 | Disponibilidad | Falla nodo cómputo en horario atención | Disponibilidad `>= 99,5 %` mensual | Multi-AZ (AZ-A/AZ-B), ALB, auto scaling, health checks |
| ASR-3 | Rendimiento | Especialista abre historial legado | `p95 <= 2 s` caché; `<= 6 s` consulta directa | Capa anticorrupción + caché lectura CQRS |
| ASR-4 | Rendimiento | Pico de 40 teleconsultas simultáneas | Señalización `<= 300 ms`; sin servidor video propio | Servicio gestionado WebRTC regional |
| ASR-5 | Seguridad | Usuario sin rol clínico intenta leer historia | Denegación `100 %` + registro auditoría | ABAC en servicio + TLS/KMS + S3 Object Lock |
| ASR-6 | Seguridad | Acceso clínico fuera del país | Bloqueo por política; residencia garantizada | Región única AWS sin réplicas cross-region |
| ASR-7 | Mantenibilidad | Cambio de proveedor del legado | Aislado en `<= 1` módulo sin tocar dominio | Puerto `HistorialClinicoPort` + adaptador ACL |
| ASR-8 | Mantenibilidad | Nuevo canal (app móvil paciente) | Reutilizar `>= 80 %` servicios de dominio | Monolito modular con límites `ArchUnit` |
| ASR-9 | Mantenibilidad | Despliegue sin ventana mantenimiento | Automatizado; rollback `<= 10 min` | CI/CD + IaC + observabilidad Micrometer/OTel |

## 3. Diagrama de Contexto (`Modelo`)
```mermaid
flowchart LR
  subgraph agg_sistema_de_salud["sistema de salud<br/><i>Límite de Sistema</i>"]
    redsalud("RedSalud Andina<br/><i>Sistema</i>")
  end
  people("Pacientes<br/><i>Persona</i>")
  specialist("Especialistas<br/><i>Persona</i>")
  manager("Gestor y auditor<br/><i>Persona</i>")
  old("Legado clínico<br/><i>Sistema Externo</i>")
  video("Video gestionado nacional<br/><i>Sistema Externo</i>")
  pharmacy("Firma y farmacia<br/><i>Sistema Externo</i>")
  idp("Proveedor de identidad<br/><i>Sistema Externo</i>")
  medicos_rurales("Médicos rurales<br/><i>Persona</i>")
  people -->|"Recibir atención"| redsalud
  specialist -->|"Atención y receta"| redsalud
  manager -->|"Control y evidencia"| redsalud
  redsalud -->|"Consultar historial"| old
  redsalud -->|"Organizar teleconsulta"| video
  redsalud -->|"Enviar receta firmada"| pharmacy
  redsalud -->|"Autenticar [OIDC]"| idp
  medicos_rurales -->|"Registrar y agendar"| redsalud
```

## 4. Contenedores
```mermaid
flowchart LR
  subgraph agg_RedSalud_Andina___L_mite_de_Sistema["RedSalud Andina - Límite de Sistema<br/><i>Límite de Sistema</i>"]
    web("Cliente web<br/><i>Contenedor</i>")
    backend("Monolito modular · Colombia<br/><i>Contenedor</i>")
    db[("PostgreSQL<br/><i>Base de Datos</i>")]
  end
  old("Legado clínico on-premise<br/><i>Sistema Externo</i>")
  video("Video WebRTC gestionado<br/><i>Sistema Externo</i>")
  pharmacy("Firma y farmacia del centro<br/><i>Sistema Externo</i>")
  idp("Proveedor de identidad<br/><i>Sistema Externo</i>")
  pacientes("Pacientes<br/><i>Persona</i>")
  medicos_rurales("Médicos rurales<br/><i>Persona</i>")
  especialistas("Especialistas<br/><i>Persona</i>")
  gestor_y_auditor("Gestor y auditor<br/><i>Persona</i>")
  backend -->|"SOAP nocturno / solo lectura"| old
  backend -->|"OIDC"| idp
  backend -->|"Firma y entrega"| pharmacy
  backend -->|"Sesiones"| video
  web -->|"Audio/video [WebRTC]"| video
  pacientes -->|"Recibir atención"| web
  medicos_rurales -->|"Registrar y agendar"| web
  especialistas -->|"Atender y emitir recetas"| web
  gestor_y_auditor -->|"Revisar gestión y evidencia"| web
  web -->|"TLS / reintento idempotente"| backend
  backend -->|"Datos privados [SQL/TLS]"| db
```

## 5. Vista Lógica (Hexagonal)
```mermaid
flowchart LR
  in["Adaptadores de entrada<br/><i>Componente</i>"]
  ag["Agenda e interconsulta<br/><i>Componente</i>"]
  ep["Episodio clínico<br/><i>Componente</i>"]
  rx["Receta y farmacia<br/><i>Componente</i>"]
  seg["Seguimiento e indicadores<br/><i>Componente</i>"]
  sync["Sincronización idempotente<br/><i>Componente</i>"]
  audit["Autorización y auditoría<br/><i>Componente</i>"]
  ports["Puertos de salida<br/><i>Componente</i>"]
  adap["Adaptadores externos<br/><i>Componente</i>"]
  in -->|"Invoca casos de uso"| ag
  in -->|"Invoca casos de uso"| ep
  in -->|"Invoca casos de uso"| rx
  in -->|"Recibe lote local"| sync
  sync -->|"Valida cupos online"| ag
  sync -->|"Aplica versión válida"| ep
  ag -->|"Evento interno + outbox"| seg
  ep -->|"Autoriza y registra"| audit
  rx -->|"Autoriza y registra"| audit
  ag -->|"Usa contratos"| ports
  ep -->|"Usa contratos"| ports
  rx -->|"Usa contratos"| ports
  ports -->|"Implementa contratos"| adap
```

## 6. Vista Física y de Datos
```mermaid
flowchart LR
  pwa("Cliente offline-first<br/><i>Contenedor</i>")
  mono("Aplicación única<br/><i>Contenedor</i>")
  db[("PostgreSQL<br/><i>Base de Datos</i>")]
  obj("Objetos clínicos cifrados<br/><i>Sistema</i>")
  acl["Adaptador anticorrupción<br/><i>Componente</i>"]
  old("Legado on-premise<br/><i>Sistema Externo</i>")
  video("Proveedor WebRTC<br/><i>Sistema Externo</i>")
  evi("Evidencia inmutable<br/><i>Sistema</i>")
  pwa -->|"REST + operationId [TLS]"| mono
  mono -->|"Transacciones [SQL/TLS]"| db
  mono -->|"Objetos privados [TLS]"| obj
  mono -->|"Puerto HistoriaClinica"| acl
  acl -->|"SOAP nocturno / BD RO"| old
  acl -->|"Proyección de lectura"| db
  mono -->|"Sesiones y asistencia"| video
  pwa -->|"Medios [WebRTC]"| video
  mono -->|"Outbox durable [TLS]"| evi
```

## 7. Vista de Despliegue (Multi-AZ · Región Única)
```mermaid
flowchart LR
  edge("Centros y especialistas<br/><i>Sistema Externo</i>")
  lb("Entrada HTTPS - ALB<br/><i>Sistema</i>")
  a("Monolito AZ-A<br/><i>Contenedor</i>")
  b("Monolito AZ-B<br/><i>Contenedor</i>")
  rds[("RDS PostgreSQL Multi-AZ<br/><i>Base de Datos</i>")]
  s3("S3 privado + Object Lock<br/><i>Sistema</i>")
  ops("Identidad, KMS y observabilidad<br/><i>Sistema</i>")
  onprem("Legado y video nacionales<br/><i>Sistema Externo</i>")
  edge -->|"HTTPS"| lb
  lb -->|"Distribuye tráfico"| a
  lb -->|"Distribuye tráfico"| b
  a -->|"SQL/TLS"| rds
  b -->|"SQL/TLS"| rds
  a -->|"Acceso privado"| s3
  b -->|"Acceso privado"| s3
  a -->|"Identidad y operación"| ops
  b -->|"Identidad y operación"| ops
  a -->|"Integración privada"| onprem
  b -->|"Integración privada"| onprem
```

## 8. Componentes del Backend
```mermaid
flowchart LR
  subgraph agg_Backend_monol_tico["Backend monolítico · RedSalud Andina<br/><i>Límite de Contenedor</i>"]
    identidad["Identidad y autorización<br/><i>Componente</i>"]
    agenda["Agenda y reservas<br/><i>Componente</i>"]
    receta["Receta electrónica<br/><i>Componente</i>"]
    historial["Historial clínico<br/><i>Componente</i>"]
    sincronizacion["Sincronización offline<br/><i>Componente</i>"]
    auditoria["Auditoría<br/><i>Componente</i>"]
    adaptador["Adaptador del legado<br/><i>Componente</i>"]
    episodio["Episodio de teleconsulta<br/><i>Componente</i>"]
    interconsultas["Interconsultas<br/><i>Componente</i>"]
    indicadores["Indicadores<br/><i>Componente</i>"]
  end
  sincronizacion -->|"Aplica reservas offline"| agenda
  sincronizacion -->|"Aplica adjuntos offline"| episodio
  agenda -->|"ReservaEnConflicto [evento]"| sincronizacion
  agenda -->|"ReservaConfirmada [evento]"| interconsultas
  episodio -->|"TeleconsultaAtendida [evento]"| interconsultas
  historial -->|"obtenerHistorial"| adaptador
  historial -->|"HistorialConsultado [evento]"| auditoria
  identidad -->|"AccesoDenegado [evento]"| auditoria
  interconsultas -->|"Cambios de estado [evento]"| indicadores
  receta -->|"RecetaEmitida [evento]"| indicadores
  interconsultas -->|"Alerta de pendiente"| interconsultas
```

## 9. Modelo Lógico de Base de Datos (MER)
```mermaid
flowchart LR
  subgraph agg_PostgreSQL___modelo_l_gico_de_RedSalud["PostgreSQL - modelo lógico de RedSalud<br/><i>Esquema</i>"]
    db_centro(("Centro de atención<br/><i>Entidad Fuerte</i>"))
    db_usuario(("Usuario<br/><i>Entidad Fuerte</i>"))
    db_paciente(("Paciente<br/><i>Entidad Fuerte</i>"))
    db_cupo(("Cupo de especialista<br/><i>Entidad Fuerte</i>"))
    db_interconsulta(("Interconsulta<br/><i>Entidad Fuerte</i>"))
    db_cita(("Cita<br/><i>Entidad Fuerte</i>"))
    db_episodio(("Episodio clínico<br/><i>Entidad Fuerte</i>"))
    db_adjunto(("Adjunto clínico<br/><i>Entidad Fuerte</i>"))
    db_receta(("Receta electrónica<br/><i>Entidad Fuerte</i>"))
    db_historial(("Historial del legado<br/><i>Entidad Fuerte</i>"))
    db_auditoria(("Acceso clínico auditado<br/><i>Entidad Fuerte</i>"))
    db_operacion(("Operación de sincronización<br/><i>Entidad Fuerte</i>"))
  end
  db_usuario -->|"Especialista publica (0,N)"| db_cupo
  db_usuario -->|"Médico solicita (0,N)"| db_interconsulta
  db_paciente -->|"Recibe solicitudes (0,N)"| db_interconsulta
  db_centro -->|"Origina (0,N)"| db_interconsulta
  db_interconsulta -->|"Historial de reservas (0,N)"| db_cita
  db_cupo -->|"Reservas históricas (0,N)"| db_cita
  db_cita -->|"Genera atención (0,1)"| db_episodio
  db_episodio -->|"Tiene documentos (0,N)"| db_adjunto
  db_episodio -->|"Genera recetas (0,N)"| db_receta
  db_usuario -->|"Especialista prescribe (0,N)"| db_receta
  db_paciente -->|"Tiene antecedentes (0,N)"| db_historial
  db_usuario -->|"Deja evidencia (0,N)"| db_auditoria
  db_usuario -->|"Envía operaciones (0,N)"| db_operacion
```
