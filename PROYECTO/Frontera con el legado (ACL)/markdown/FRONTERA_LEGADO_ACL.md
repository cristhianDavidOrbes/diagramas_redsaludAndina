# Frontera con el Legado: Adaptador Anticorrupción (Sesión 2)

**Proyecto ProcessFlow:** `redSaludAndina_ACL_Legado` · **ADR asociado:** `ADR-002` · **RF cubierto:** `RF-06`

## 1. Decisión arquitectónica y métricas objetivo
- **Regla dura:** el sistema legado no se modifica y es la fuente de verdad del historial clínico (expone únicamente SOAP nocturno y base de datos de solo lectura). Nuestra vista es una copia de lectura con caducidad visible.
- **Patrón:** CQRS con *read model* propio (`Caffeine L1` + `ElastiCache/DynamoDB L2`), puerto de dominio `HistorialClinicoPort`, adaptador SOAP con `WebServiceTemplate` + timeouts + `Resilience4j` (`@CircuitBreaker`) y cabecera `X-Data-Freshness` en `GET /historial/{id}`.
- **Criterios de aceptación:** `p95 <= 2 s` con caché; `<= 6 s` en consulta directa; **ejercicio detonante:** al apagar el mock SOAP el endpoint sigue respondiendo desde el *read model* con frescura visible (nunca HTTP `500`); cambio de proveedor aislado en `<= 1` módulo (`legado.infraestructura.salida.soap`).

## 2. Mapa de traducción: Modelo Legado (SOAP) vs. Modelo Canónico
| Campo en Legado SOAP / BD RO | Campo Canónico (`HistorialClinicoCanonico`) | Regla de traducción en `AdaptadorSoapACL` |
| --- | --- | --- |
| `COD_PAC_HIST` (string propietario) | `pacienteId` (`UUID`) | Traducido en ACL mediante tabla de correlación; nunca se filtra código legado al dominio |
| `XML_RESUMEN_ANT` / `OBS_CLN` | `resumenAntecedentes` (`String` minimizado) | Normalizado y minimizado a datos pertinentes; se descartan campos administrativos |
| `FEC_ULT_CARGA_NOCT` | `fechaSincronizacion` (`Instant`) | Publicado en cabecera HTTP `X-Data-Freshness` |
| Estado circuito / origen | `degradadoDesdeCache` (`boolean`) | `true` cuando `CircuitBreaker` está abierto o SOAP fuera de ventana |

## 3. Arquitectura de la Capa Anticorrupción (C4)
```mermaid
flowchart LR
  subgraph Monolito_Modular___M_dulo_Integraci_n_Legado["Monolito Modular · Módulo Integración Legado<br/><i>Límite de Contenedor</i>"]
    acl_ctrl["HistorialLegadoController<br/><i>Componente</i>"]
    acl_svc["ConsultarHistorialService<br/><i>Componente</i>"]
    acl_cache_repo["ReadModelHistorialRepo<br/><i>Componente</i>"]
    acl_port["HistorialClinicoPort<br/><i>Componente</i>"]
    acl_soap["AdaptadorSoapACL<br/><i>Componente</i>"]
  end
  acl_esp("Especialista<br/><i>Persona</i>")
  acl_aud[("Auditoría Lecturas Clínicas<br/><i>Base de Datos</i>")]
  acl_cache_db[("Caffeine L1 + ElastiCache L2<br/><i>Base de Datos</i>")]
  acl_legacy("Legado SOAP + BD Solo Lectura<br/><i>Sistema Externo</i>")
  acl_esp -->|"GET /historial/{id} [HTTPS]"| acl_ctrl
  acl_ctrl -->|"Consulta canónica"| acl_svc
  acl_ctrl -->|"Evento lectura + motivo"| acl_aud
  acl_svc -->|"1. Lee read model (<= 2 s)"| acl_cache_repo
  acl_svc -->|"2. Fallback miss (<= 6 s)"| acl_port
  acl_port -->|"Implementa puerto ACL"| acl_soap
  acl_cache_repo -->|"L1/L2 + TTL + X-Data-Freshness"| acl_cache_db
  acl_soap -->|"SOAP / BD RO [Resilience4j]"| acl_legacy
```

## 4. Modelo Táctico DDD y Event Storming
```mermaid
flowchart LR
  subgraph Agregado_HistorialVista["Agregado HistorialVista<br/><i>Agregado</i>"]
    ad_root(("HistorialCanónico<br/><i>Raíz de Agregado</i>"))
    ad_vo(("MarcaFrescura<br/><i>Objeto de Valor</i>"))
    ad_repo(("CacheTwoLevelRepo<br/><i>Repositorio</i>"))
  end
  subgraph Frontera_Anticorrupci_n["Frontera Anticorrupción<br/><i>Contexto Delimitado</i>"]
    ad_acl["ACL SOAP Nocturno<br/><i>Capa Anticorrupción (ACL)</i>"]
    ad_reg(("Regla Legado Verdad<br/><i>Regla de Negocio</i>"))
  end
  ad_act("Especialista<br/><i>Actor</i>")
  ad_cmd1(("ConsultarHistorial<br/><i>Comando</i>"))
  ad_ev1(("HistorialCacheExpirado<br/><i>Evento</i>"))
  ad_pol(("PolíticaFallbackACL<br/><i>Política</i>"))
  ad_cmd2(("ConsultarLegadoSOAP<br/><i>Comando</i>"))
  ad_ev2(("HistorialEntregado<br/><i>Evento</i>"))
  ad_sys("Legado SOAP + BD RO<br/><i>Sistema Externo</i>")
  ad_act -->|"solicita"| ad_cmd1
  ad_cmd1 -->|"cache miss"| ad_ev1
  ad_cmd1 -->|"lee proyección"| ad_root
  ad_ev1 -->|"activa"| ad_pol
  ad_pol -->|"dispara"| ad_cmd2
  ad_cmd2 -->|"degrada o refresca"| ad_ev2
  ad_cmd2 -->|"invoca puerto"| ad_acl
  ad_root -->|"expone TTL"| ad_vo
  ad_root -->|"persiste L1/L2"| ad_repo
  ad_acl -->|"aplica"| ad_reg
  ad_acl -->|"SOAP / BD RO"| ad_sys
```

## 5. Secuencia: Caché, Fallback y Ejercicio Detonante (Mock SOAP Apagado)
```mermaid
sequenceDiagram
    autonumber
    participant E as Especialista
    participant API as GET /historial/{id}
    participant ABAC as Autorizador ABAC
    participant RM as Cache L1 Caffeine + L2 ElastiCache
    participant ACL as Adaptador SOAP (Resilience4j)
    participant LEG as Legado SOAP Nocturno + BD RO
    participant AUD as Auditoría S3 Object Lock

    E->>API: GET /historial/{pacienteId}?motivo=teleconsulta
    API->>ABAC: ¿Rol clínico + centro + relación?
    ABAC-->>API: Permitido
    API->>RM: Buscar read model canónico (TTL vigente)
    alt Cache Hit (p95 <= 2 s)
        RM-->>API: HistorialCanónico + fechaSincronizacion
        API->>AUD: Registrar lectura clínica (usuario, fecha, recurso, motivo)
        API-->>E: 200 OK + X-Data-Freshness: fresh
    else Cache Miss / TTL expirado (<= 6 s)
        API->>ACL: HistorialClinicoPort.consultar(pacienteId)
        ACL->>LEG: WebServiceTemplate (timeout + CircuitBreaker)
        alt Legado responde en ventana autorizada
            LEG-->>ACL: Respuesta XML/SOAP legado
            ACL->>ACL: Traducir a modelo canónico (sin filtrar DTO SOAP)
            ACL-->>API: HistorialCanónico actualizado
            API->>RM: Actualizar caché L1/L2 con TTL explícito
            API->>AUD: Registrar lectura clínica
            API-->>E: 200 OK + X-Data-Freshness: live
        else Ejercicio detonante: Mock SOAP apagado / timeout
            LEG--xACL: Error / Timeout
            ACL-->>API: CircuitBreaker OPEN -> fallback read model
            API->>AUD: Registrar lectura degradada
            API-->>E: 200 OK (nunca 500) + X-Data-Freshness: stale
        end
    end
```
