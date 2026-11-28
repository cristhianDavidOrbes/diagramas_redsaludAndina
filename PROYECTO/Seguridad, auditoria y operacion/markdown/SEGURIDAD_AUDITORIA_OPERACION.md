# Seguridad, Auditoría Demostrable y Operación (Sesión 4)

**Proyecto ProcessFlow:** `redSaludAndina_Seguridad_Auditoria_Operacion` · **ADRs asociados:** `ADR-004`, `ADR-005` · **RF cubiertos:** `RF-01`, `RF-08`, `RF-10`

## 1. Controles de seguridad, residencia y cumplimiento
| Dimensión | Mecanismo arquitectónico | Evidencia de aceptación |
| --- | --- | --- |
| **Asignación de roles (IAM)** | **Admin de Red / TI** aprovisiona usuarios y asigna roles (`MEDICO_RURAL`, `ESPECIALISTA`, `AUDITOR`, `ADMIN`) y atributos (`centro_id`, `especialidad`) en **Amazon Cognito + DynamoDB Perfiles** | Ningún usuario se autoasigna roles; perfiles gestionados con consistencia fuerte |
| **Autenticación (AuthN)** | El usuario inicia sesión con **OIDC + MFA** en **Amazon Cognito** y obtiene un **JWT firmado**; en el backend, **OAuth2 Resource Server (ACL)** verifica firma JWKS y traduce claims a `PrincipalClínico` | Tokens inválidos o sin firma JWKS rechazados en frontera (`401`); claims aislados en `identidad.infraestructura` |
| **Autorización ABAC (AuthZ)** | `@PreAuthorize` + `AuthorizationManager` propio por **rol y atributo** (`centro`, `especialidad`, `relación con paciente`, `motivo`) evaluado en **capa de servicio** | Test negativo: usuario administrativo recibe `403` y el `100 %` de denegaciones queda auditado |
| **Cifrado y residencia** | TLS en tránsito + AWS KMS con claves por contexto y rotación; bucket S3 en región única sin replicación cross-region | Configuración + test de política de bucket bloqueando réplicas externas |
| **Auditoría demostrable** | Evento por cada lectura clínica (`usuario`, `fecha`, `recurso`, `motivo`, `permitido`) en almacén *append-only* `S3 Object Lock (Compliance)` en transacción independiente | Script reproducible + salida versionada: *"quién vio el documento X y con qué motivo"* |
| **Observabilidad y SLO** | `Micrometer` + `OpenTelemetry` + `Actuator` + `Prometheus` + export analítico S3/Athena | Dashboard mínimo sosteniendo SLO `>= 99,5 %` mensual |
| **Operación CI/CD** | Pipeline automatizado con `ArchUnit`, `Testcontainers`, cobertura `>= 70 %` e IaC | Despliegue sin ventana y *rollback* `<= 10 min` |

## 2. Arquitectura de Seguridad, Auditoría y Operación (C4)
```mermaid
flowchart LR
  subgraph Regi_n_AWS__nica___Seguridad__Auditor_a_y_Operaci_n["Región AWS Única · Seguridad, Auditoría y Operación<br/><i>Límite de Contenedor</i>"]
    sec_oauth["OAuth2 Resource Server (ACL)<br/><i>Componente</i>"]
    sec_abac["Autorizador ABAC (Servicio)<br/><i>Componente</i>"]
    sec_int["Interceptor Auditoría Clínica<br/><i>Componente</i>"]
    sec_obs["Micrometer + OpenTelemetry<br/><i>Componente</i>"]
    sec_rep["Reporte Auditoría Reproducible<br/><i>Componente</i>"]
  end
  sec_admin_red("Admin de Red / TI<br/><i>Persona</i>")
  sec_usr("Usuario Clínico / Admin<br/><i>Persona</i>")
  sec_cog("Cognito + DynamoDB Perfiles<br/><i>Sistema Externo</i>")
  sec_kms[("AWS KMS + S3 Adjuntos<br/><i>Base de Datos</i>")]
  sec_worm[("S3 Object Lock (Compliance)<br/><i>Base de Datos</i>")]
  sec_aud_actor("Auditor Clínico<br/><i>Persona</i>")
  sec_cicd("CI/CD + IaC (Rollback <= 10m)<br/><i>Sistema Externo</i>")
  sec_admin_red -->|"1. Asigna roles y centro"| sec_cog
  sec_usr -->|"2. Autentica OIDC+MFA (JWT)"| sec_cog
  sec_usr -->|"3. HTTPS + Bearer JWT [TLS]"| sec_oauth
  sec_oauth -->|"4. Valida firma JWKS y perfil"| sec_cog
  sec_oauth -->|"5. PrincipalClínico"| sec_abac
  sec_abac -->|"Cifrado KMS por contexto"| sec_kms
  sec_abac -->|"6. Permite/deniega 403"| sec_int
  sec_int -->|"Append-only Compliance"| sec_worm
  sec_rep -->|"Lee evidencia inmutable"| sec_worm
  sec_aud_actor -->|"Consulta quién vio DOC-X"| sec_rep
  sec_obs -->|"SLO 99,5% + rollback <=10m"| sec_cicd
```

## 3. Modelo Táctico DDD y Event Storming (`SesiónABAC` + `TrazaAcceso`)
```mermaid
flowchart LR
  subgraph Agregado_Sesi_nABAC["Agregado SesiónABAC<br/><i>Agregado</i>"]
    sd_id(("PrincipalClínico<br/><i>Raíz de Agregado</i>"))
    sd_abac(("AutorizadorABAC<br/><i>Servicio de Dominio</i>"))
    sd_acl["ACL OIDC / OAuth2<br/><i>Capa Anticorrupción (ACL)</i>"]
  end
  subgraph Agregado_TrazaAcceso["Agregado TrazaAcceso<br/><i>Agregado</i>"]
    sd_traza(("TrazaAccesoClínico<br/><i>Raíz de Agregado</i>"))
    sd_worm(("S3ObjectLockRepo<br/><i>Repositorio</i>"))
    sd_reg(("Regla Residencia<br/><i>Regla de Negocio</i>"))
  end
  sd_act("Usuario Sin Rol Clínico<br/><i>Actor</i>")
  sd_cmd1(("SolicitarLecturaClínica<br/><i>Comando</i>"))
  sd_ev1(("AccesoClinicoDenegado<br/><i>Evento</i>"))
  sd_pol(("PolíticaAuditoriaObligatoria<br/><i>Política</i>"))
  sd_cmd2(("ArchivarTrazaWORM<br/><i>Comando</i>"))
  sd_ev2(("TrazaWORMArchivada<br/><i>Evento</i>"))
  sd_vis(("Reporte Quien Vio Que<br/><i>Vista</i>"))
  sd_aud("Auditor Clínico<br/><i>Actor</i>")
  sd_admin_red("Admin de Red / TI<br/><i>Actor</i>")
  sd_cognito("Cognito OIDC + Perfiles<br/><i>Sistema Externo</i>")
  sd_act -->|"envía JWT e intenta leer"| sd_cmd1
  sd_cmd1 -->|"deniega 100%"| sd_ev1
  sd_cmd1 -->|"autentica y evalúa ABAC"| sd_id
  sd_ev1 -->|"dispara"| sd_pol
  sd_pol -->|"ordena"| sd_cmd2
  sd_cmd2 -->|"confirma"| sd_ev2
  sd_cmd2 -->|"registra inmutable"| sd_traza
  sd_ev2 -->|"alimenta"| sd_vis
  sd_vis -->|"entrega reporte"| sd_aud
  sd_id -->|"autoriza ABAC"| sd_abac
  sd_id -->|"traduce JWT"| sd_acl
  sd_admin_red -->|"asigna roles y centro"| sd_cognito
  sd_cognito -->|"autentica OIDC+MFA (JWT)"| sd_acl
  sd_traza -->|"persiste WORM"| sd_worm
  sd_traza -->|"garantiza región"| sd_reg
```

## 4. Secuencia: Ejercicio Detonante — Denegación Auditada y Reporte Reproducible
```mermaid
sequenceDiagram
    autonumber
    participant AD as Admin de Red / TI
    participant U as Usuario Administrativo (sin rol clínico)
    participant C as Amazon Cognito (OIDC+MFA) + DynamoDB Perfiles
    participant O as OAuth2 Resource Server (ACL OIDC)
    participant S as Servicio Clínico (@PreAuthorize + AuthorizationManager)
    participant I as Interceptor Auditoría (REQUIRES_NEW)
    participant W as S3 Object Lock (Compliance WORM)
    participant A as Auditor Clínico
    participant R as Reporte Reproducible Versionado

    Note over AD,C: 1) Gobernanza IAM — Admin de Red / TI asigna roles y atributos
    AD->>C: Aprovisiona usuario + asigna rol (ADMIN / MEDICO_RURAL / ESPECIALISTA) y centro
    Note over U,C: 2) Autenticación (AuthN) — Identidad verificada antes de llamar al API
    U->>C: Login OIDC + MFA (usuario, clave, 2FA)
    C-->>U: Emite JWT firmado (claims: sub, rol=ADMIN, centro_id, especialidad)
    Note over U,W: 3) Autorización ABAC en servicio (AuthZ) — Región AWS única + TLS + KMS
    U->>O: GET /api/v1/historial/DOC-X (Authorization: Bearer JWT)
    O->>C: Validar firma criptográfica JWT (JWKS) y atributos del perfil
    C-->>O: PrincipalClínico(rol=ADMIN, sin relación asistencial)
    O->>S: Invoca caso de uso con contexto de seguridad autenticado
    S->>S: Evalúa AuthorizationManager ABAC en capa de servicio (no controlador)
    S->>I: Registrar intento denegado (usuario, fecha, DOC-X, motivo, permitido=false)
    I->>W: PutObject ObjectLockMode=COMPLIANCE (append-only, sin rollback)
    W-->>I: Evidencia inmutable persistida
    S-->>U: 403 Forbidden (100 % denegaciones registradas)
    Note over A,R: 4) Demostración verificable de auditoría (Sesión 4)
    A->>R: Ejecutar script reporte "¿Quién vio DOC-X y con qué motivo?"
    R->>W: Consultar trazas inmutables de DOC-X
    W-->>R: Accesos permitidos + intento denegado de ADMIN
    R-->>A: Reporte reproducible versionado en repositorio
```

## 5. Mapa Regulatorio y Políticas del Dato Clínico
```mermaid
mindmap
  root((Regulatorio y políticas RedSalud Andina))
    Residencia del dato
      Datos clínicos no salen del país
      Región única AWS sin réplicas externas
      Bucket adjuntos sin replicación cross-region
    PII y datos sensibles
      Identificador paciente, adjuntos y receta firmada
      Médicos rurales, especialistas y roles
      Minimización en vista de lectura legado
    Control de acceso ABAC
      Autorización por rol y atributo (centro, especialidad, relación)
      Evaluación en capa de servicio (@PreAuthorize + AuthorizationManager)
      Propiedad del dato por contexto delimitado
    Cifrado y claves
      TLS en tránsito
      KMS en reposo con claves por contexto y rotación
    Auditoría y trazabilidad
      Evento por cada lectura clínica y 100% de denegaciones
      Almacén append-only S3 Object Lock Compliance
      Reporte reproducible: quién vio este documento y con qué motivo
    Operación y resiliencia
      Micrometer + OpenTelemetry + Actuator (SLO 99,5%)
      CI/CD + IaC con rollback <= 10 min
```
