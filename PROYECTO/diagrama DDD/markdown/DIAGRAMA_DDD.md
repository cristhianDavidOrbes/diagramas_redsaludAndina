# RedSalud Andina — Diseño Estratégico y Táctico DDD / Event Storming

**Proyecto:** `redSaludAndina_DDD` · **Verificación geométrica:** `cruces: 0`, `solape: 0`, `sobreCaja: 0`, `etiquetasTapadas: 0`, aristas duplicadas limpiadas (`0` duplicados) y nodo raíz corregido (`Interconsulta`).

## 1. Mapa de Subdominios, 9 Contextos Delimitados y Agregados
| Subdominio | Contexto Delimitado | Módulo Spring Boot | Agregado y Raíz | Elementos tácticos y Mapa de Contexto |
| --- | --- | --- | --- | --- |
| **Core · Atención Teleclínica** | `Interconsulta` | `interconsulta` | `Agregado Interconsulta` (`Interconsulta` `@Version`) | `EstadoDerivación` (VO), `RepoInterconsultas`, `OHS Interconsulta API` (`⊞` → `01`) |
| **Core · Atención Teleclínica** | `Agenda` | `agenda` | `Agregado CupoCita` (`CupoEspecialista` `@Version`) | `CitaTelemedicina` (Entidad), `RepoCuposOptimista`, `OHS Agenda API` |
| **Core · Atención Teleclínica** | `Episodio Clínico` | `episodio` | `Agregado Episodio` (`EpisodioClínico`) | `AdjuntoCifradoS3` (Entidad KMS), `RepoEpisodios`, `OHS Episodio API` (`⊞` → `02`) |
| **Core · Atención Teleclínica** | `Receta Electrónica` | `receta` | `Agregado Receta` (`RecetaMédica`) | `FirmaKMS` (VO), `RepoRecetaOutbox`, `PL Contrato SQS` (`⊞` → `03`) |
| **Soporte · Sync y Analítica** | `Sincronización Offline` | `sincronizacion` | `Agregado LoteSync` (`LoteSincronización`) | `ClaveIdempotencia` (ULID), `ResolutorConflictos`, `Regla LWW y Rechazo`, `C/P Clínica-Sync` |
| **Soporte · Sync y Analítica** | `Analítica de Uso` | `analitica` | `Agregado MétricaUso` (`TableroAdopción`) | `Vista SLO 99.5%` (sobre réplica/export S3 + Athena, sin contaminar OLTP) |
| **Genérico · Acceso, Legado y Aud.** | `Identidad y Acceso` | `identidad` | `Agregado SesiónABAC` (`PrincipalClínico`) | `AutorizadorABAC` (`@PreAuthorize`), `ACL OIDC / OAuth2` |
| **Genérico · Acceso, Legado y Aud.** | `Integración Legado` | `legado` | `Agregado HistorialVista` (`HistorialCanónico`) | `CacheTwoLevelRepo` (Caffeine + ElastiCache), `ACL SOAP Nocturno` (`⊞` → `04`) |
| **Genérico · Acceso, Legado y Aud.** | `Auditoría` | `auditoria` | `Agregado TrazaAcceso` (`TrazaAccesoClínico`) | `S3ObjectLockRepo` (append-only), `Object Lock WORM` |

## 2. Vista Principal `Modelo` (DDD)
```mermaid
flowchart LR
  subgraph Core___Atenci_n_Telecl_nica["Core · Atención Teleclínica<br/><i>Subdominio</i>"]
  end
  subgraph Interconsulta["Interconsulta<br/><i>Contexto Delimitado</i>"]
    c_inter_ohs(("OHS Interconsulta API<br/><i>Servicio de Host Abierto (OHS)</i>"))
  end
  subgraph Agregado_Interconsulta["Agregado Interconsulta<br/><i>Agregado</i>"]
    c_inter_root(("Interconsulta<br/><i>Raíz de Agregado</i>"))
    c_inter_vo(("EstadoDerivación<br/><i>Objeto de Valor</i>"))
    c_inter_repo(("RepoInterconsultas<br/><i>Repositorio</i>"))
  end
  subgraph Agenda["Agenda<br/><i>Contexto Delimitado</i>"]
    c_ag_ohs(("OHS Agenda API<br/><i>Servicio de Host Abierto (OHS)</i>"))
  end
  subgraph Agregado_CupoCita["Agregado CupoCita<br/><i>Agregado</i>"]
    c_ag_root(("CupoEspecialista<br/><i>Raíz de Agregado</i>"))
    c_ag_ent(("CitaTelemedicina<br/><i>Entidad</i>"))
    c_ag_repo(("RepoCuposOptimista<br/><i>Repositorio</i>"))
  end
  subgraph Episodio_Cl_nico["Episodio Clínico<br/><i>Contexto Delimitado</i>"]
    c_ep_ohs(("OHS Episodio API<br/><i>Servicio de Host Abierto (OHS)</i>"))
  end
  subgraph Agregado_Episodio["Agregado Episodio<br/><i>Agregado</i>"]
    c_ep_root(("EpisodioClínico<br/><i>Raíz de Agregado</i>"))
    c_ep_ent(("AdjuntoCifradoS3<br/><i>Entidad</i>"))
    c_ep_repo(("RepoEpisodios<br/><i>Repositorio</i>"))
  end
  subgraph Receta_Electr_nica["Receta Electrónica<br/><i>Contexto Delimitado</i>"]
    c_rec_pl["PL Contrato SQS<br/><i>Lenguaje Publicado (PL)</i>"]
  end
  subgraph Agregado_Receta["Agregado Receta<br/><i>Agregado</i>"]
    c_rec_root(("RecetaMédica<br/><i>Raíz de Agregado</i>"))
    c_rec_vo(("FirmaKMS<br/><i>Objeto de Valor</i>"))
    c_rec_repo(("RepoRecetaOutbox<br/><i>Repositorio</i>"))
  end
  subgraph Soporte___Sync_y_Anal_tica["Soporte · Sync y Analítica<br/><i>Subdominio</i>"]
  end
  subgraph Sincronizaci_n_Offline["Sincronización Offline<br/><i>Contexto Delimitado</i>"]
    s_sy_reg(("Regla LWW y Rechazo<br/><i>Regla de Negocio</i>"))
  end
  subgraph Agregado_LoteSync["Agregado LoteSync<br/><i>Agregado</i>"]
    s_sy_root(("LoteSincronización<br/><i>Raíz de Agregado</i>"))
    s_sy_vo(("ClaveIdempotencia<br/><i>Objeto de Valor</i>"))
    s_sy_srv(("ResolutorConflictos<br/><i>Servicio de Dominio</i>"))
  end
  subgraph Anal_tica_de_Uso["Analítica de Uso<br/><i>Contexto Delimitado</i>"]
    s_an_vista(("Vista SLO 99.5%<br/><i>Vista</i>"))
  end
  subgraph Agregado_M_tricaUso["Agregado MétricaUso<br/><i>Agregado</i>"]
    s_an_root(("TableroAdopción<br/><i>Raíz de Agregado</i>"))
  end
  subgraph Gen_rico___Acceso__Legado_y_Aud_["Genérico · Acceso, Legado y Aud.<br/><i>Subdominio</i>"]
  end
  subgraph Identidad_y_Acceso["Identidad y Acceso<br/><i>Contexto Delimitado</i>"]
    g_id_acl["ACL OIDC / OAuth2<br/><i>Capa Anticorrupción (ACL)</i>"]
  end
  subgraph Agregado_Sesi_nABAC["Agregado SesiónABAC<br/><i>Agregado</i>"]
    g_id_root(("PrincipalClínico<br/><i>Raíz de Agregado</i>"))
    g_id_srv(("AutorizadorABAC<br/><i>Servicio de Dominio</i>"))
  end
  subgraph Integraci_n_Legado["Integración Legado<br/><i>Contexto Delimitado</i>"]
    g_leg_acl["ACL SOAP Nocturno<br/><i>Capa Anticorrupción (ACL)</i>"]
  end
  subgraph Agregado_HistorialVista["Agregado HistorialVista<br/><i>Agregado</i>"]
    g_leg_root(("HistorialCanónico<br/><i>Raíz de Agregado</i>"))
    g_leg_repo(("CacheTwoLevelRepo<br/><i>Repositorio</i>"))
  end
  subgraph Auditor_a["Auditoría<br/><i>Contexto Delimitado</i>"]
    g_au_reg(("Object Lock WORM<br/><i>Regla de Negocio</i>"))
  end
  subgraph Agregado_TrazaAcceso["Agregado TrazaAcceso<br/><i>Agregado</i>"]
    g_au_root(("TrazaAccesoClínico<br/><i>Raíz de Agregado</i>"))
    g_au_repo(("S3ObjectLockRepo<br/><i>Repositorio</i>"))
  end
  ext_med_rural("Médico Rural<br/><i>Actor</i>")
  ext_especialista("Especialista<br/><i>Actor</i>")
  ext_farmacia_kms("Farmacia y AWS KMS<br/><i>Sistema Externo</i>")
  ext_app_offline("App Móvil Offline<br/><i>Sistema Externo</i>")
  cm_sync_bridge["C/P Clínica-Sync<br/><i>Cliente/Proveedor</i>"]
  ext_webrtc("Video WebRTC<br/><i>Sistema Externo</i>")
  ext_oidc("Proveedor OIDC<br/><i>Sistema Externo</i>")
  ext_soap("Legado SOAP + BD RO<br/><i>Sistema Externo</i>")
  ext_auditor("Auditor Clínico<br/><i>Actor</i>")
  c_inter_root -->|"valida"| c_inter_vo
  c_inter_root -->|"persiste"| c_inter_repo
  c_inter_root -->|"publica en"| c_inter_ohs
  c_ag_root -->|"asigna"| c_ag_ent
  c_ag_root -->|"persiste @Version"| c_ag_repo
  c_ag_root -->|"expone"| c_ag_ohs
  c_inter_ohs -->|"reserva cupo"| c_ag_root
  c_ag_repo -->|"inicia atención"| c_ep_root
  c_ep_root -->|"adjunta KMS"| c_ep_ent
  c_ep_root -->|"persiste"| c_ep_repo
  c_ep_root -->|"expone"| c_ep_ohs
  c_ep_root -->|"origina receta"| c_rec_root
  c_rec_root -->|"sella firma"| c_rec_vo
  c_rec_root -->|"guarda tx outbox"| c_rec_repo
  c_rec_root -->|"publica SQS"| c_rec_pl
  s_sy_root -->|"deduplica ULID"| s_sy_vo
  s_sy_root -->|"resuelve"| s_sy_srv
  s_sy_root -->|"aplica política"| s_sy_reg
  s_an_root -->|"proyecta KPI"| s_an_vista
  s_sy_reg -->|"emite métricas"| s_an_root
  g_id_root -->|"evalúa ABAC"| g_id_srv
  g_id_root -->|"mapea claims"| g_id_acl
  g_leg_root -->|"cachea TTL"| g_leg_repo
  g_leg_root -->|"traduce SOAP"| g_leg_acl
  g_au_root -->|"graba WORM"| g_au_repo
  g_au_root -->|"exige motivo"| g_au_reg
  ext_med_rural -->|"solicita"| c_inter_root
  ext_app_offline -->|"envía lote ULID"| s_sy_root
  c_rec_pl -->|"firma KMS + SQS"| ext_farmacia_kms
  ext_especialista -->|"atiende agenda"| c_ag_root
  ext_webrtc -->|"transmite video"| c_ep_ohs
  s_sy_reg -->|"envía lote"| cm_sync_bridge
  cm_sync_bridge -->|"sincroniza campos"| c_ep_repo
  c_ag_ohs -->|"autoriza ABAC"| g_id_srv
  c_ep_ohs -->|"lee historial RO"| g_leg_root
  s_an_vista -->|"audita LWW y SLO"| g_au_root
  ext_oidc -->|"emite JWT"| g_id_acl
  ext_soap -->|"responde XML/RO"| g_leg_acl
  g_au_reg -->|"reporta trazas"| ext_auditor
```

## 3. Subvista `01 · Agenda e Interconsulta` (↔ BPMN `01 · Agendamiento`)
```mermaid
flowchart LR
  subgraph Agregado_Interconsulta["Agregado Interconsulta<br/><i>Agregado</i>"]
    s1_root_int(("Interconsulta<br/><i>Raíz de Agregado</i>"))
    s1_vo_est(("EstadoDerivación<br/><i>Objeto de Valor</i>"))
    s1_rep_int(("RepoInterconsultas<br/><i>Repositorio</i>"))
  end
  subgraph Agregado_CupoCita["Agregado CupoCita<br/><i>Agregado</i>"]
    s1_root_cupo(("CupoEspecialista<br/><i>Raíz de Agregado</i>"))
    s1_ent_cita(("CitaTelemedicina<br/><i>Entidad</i>"))
    s1_rep_cupo(("RepoCuposOptimista<br/><i>Repositorio</i>"))
  end
  s1_act_med("Médico Rural<br/><i>Actor</i>")
  s1_cmd_sol(("SolicitarInterconsulta<br/><i>Comando</i>"))
  s1_ev_sol(("InterconsultaSolicitada<br/><i>Evento</i>"))
  s1_pol_res(("PolíticaReservaCupo<br/><i>Política</i>"))
  s1_cmd_res(("ReservarCupo<br/><i>Comando</i>"))
  s1_ev_cupo(("CupoReservado<br/><i>Evento</i>"))
  s1_act_esp("Especialista<br/><i>Actor</i>")
  s1_reg_ver(("Regla Sin Doble Reserva<br/><i>Regla de Negocio</i>"))
  s1_act_med -->|"ejecuta"| s1_cmd_sol
  s1_cmd_sol -->|"produce"| s1_ev_sol
  s1_cmd_sol -->|"invoca agregado"| s1_root_int
  s1_root_int -->|"valida estado"| s1_vo_est
  s1_root_int -->|"persiste"| s1_rep_int
  s1_ev_sol -->|"activa"| s1_pol_res
  s1_pol_res -->|"emite"| s1_cmd_res
  s1_cmd_res -->|"produce"| s1_ev_cupo
  s1_cmd_res -->|"invoca agregado"| s1_root_cupo
  s1_reg_ver -->|"restringe @Version"| s1_root_cupo
  s1_root_cupo -->|"crea cita"| s1_ent_cita
  s1_root_cupo -->|"guarda tx"| s1_rep_cupo
  s1_ev_cupo -->|"notifica"| s1_act_esp
```

## 4. Subvista `02 · Episodio y Sync Offline` (↔ BPMN `02 · Atención y registro`)
```mermaid
flowchart LR
  subgraph Agregado_LoteSync["Agregado LoteSync<br/><i>Agregado</i>"]
    s2_root_lote(("LoteSincronización<br/><i>Raíz de Agregado</i>"))
    s2_vo_key(("ClaveIdempotencia<br/><i>Objeto de Valor</i>"))
    s2_srv_res(("ResolutorConflictos<br/><i>Servicio de Dominio</i>"))
  end
  subgraph Agregado_Episodio["Agregado Episodio<br/><i>Agregado</i>"]
    s2_root_ep(("EpisodioClínico<br/><i>Raíz de Agregado</i>"))
    s2_ent_adj(("AdjuntoCifradoS3<br/><i>Entidad</i>"))
    s2_rep_ep(("RepoEpisodios<br/><i>Repositorio</i>"))
  end
  s2_ext_app("App Móvil Offline<br/><i>Sistema Externo</i>")
  s2_cmd_sync(("EnviarLoteSync<br/><i>Comando</i>"))
  s2_ev_lote(("LoteValidado<br/><i>Evento</i>"))
  s2_pol_conf(("PolíticaConflicto<br/><i>Política</i>"))
  s2_cmd_ep(("GuardarEpisodio<br/><i>Comando</i>"))
  s2_ev_sync(("EpisodioSincronizado<br/><i>Evento</i>"))
  s2_vis_slo(("Vista Conflicto Auditado<br/><i>Vista</i>"))
  s2_ext_app -->|"reintenta x3"| s2_cmd_sync
  s2_cmd_sync -->|"produce"| s2_ev_lote
  s2_cmd_sync -->|"invoca agregado"| s2_root_lote
  s2_root_lote -->|"valida clave única"| s2_vo_key
  s2_root_lote -->|"resuelve LWW"| s2_srv_res
  s2_ev_lote -->|"activa"| s2_pol_conf
  s2_pol_conf -->|"emite"| s2_cmd_ep
  s2_cmd_ep -->|"produce"| s2_ev_sync
  s2_cmd_ep -->|"invoca agregado"| s2_root_ep
  s2_root_ep -->|"cifra S3 KMS"| s2_ent_adj
  s2_root_ep -->|"persiste"| s2_rep_ep
  s2_ev_sync -->|"proyecta SLA ≤15m"| s2_vis_slo
```

## 5. Subvista `03 · Receta, KMS y Outbox` (↔ BPMN `03 · Prescripción`)
```mermaid
flowchart LR
  subgraph Agregado_Receta["Agregado Receta<br/><i>Agregado</i>"]
    s3_root_rec(("RecetaMédica<br/><i>Raíz de Agregado</i>"))
    s3_vo_firma(("FirmaKMS SHA<br/><i>Objeto de Valor</i>"))
    s3_rep_out(("RepoRecetaOutbox<br/><i>Repositorio</i>"))
  end
  subgraph Agregado_OutboxRelay["Agregado OutboxRelay<br/><i>Agregado</i>"]
    s3_root_out(("MensajeOutbox<br/><i>Raíz de Agregado</i>"))
    s3_srv_sqs(("PublicadorSQSFIFO<br/><i>Servicio de Dominio</i>"))
  end
  s3_act_esp("Especialista<br/><i>Actor</i>")
  s3_cmd_emit(("EmitirReceta<br/><i>Comando</i>"))
  s3_ev_sell(("RecetaSelladaOutbox<br/><i>Evento</i>"))
  s3_pol_sqs(("PolíticaDespachoSQS<br/><i>Política</i>"))
  s3_cmd_pub(("PublicarMensajeSQS<br/><i>Comando</i>"))
  s3_ev_env(("RecetaEnviadaFarmacia<br/><i>Evento</i>"))
  s3_ext_farm("Farmacia Dispensación<br/><i>Sistema Externo</i>")
  s3_ext_kms("AWS KMS HSM<br/><i>Sistema Externo</i>")
  s3_act_esp -->|"ejecuta"| s3_cmd_emit
  s3_cmd_emit -->|"produce"| s3_ev_sell
  s3_cmd_emit -->|"invoca agregado"| s3_root_rec
  s3_root_rec -->|"sella hash"| s3_vo_firma
  s3_vo_firma -->|"firma con clave"| s3_ext_kms
  s3_root_rec -->|"guarda tx atómica"| s3_rep_out
  s3_ev_sell -->|"activa"| s3_pol_sqs
  s3_pol_sqs -->|"emite"| s3_cmd_pub
  s3_cmd_pub -->|"produce"| s3_ev_env
  s3_cmd_pub -->|"procesa outbox"| s3_root_out
  s3_root_out -->|"despacha FIFO"| s3_srv_sqs
  s3_ev_env -->|"entrega a"| s3_ext_farm
```

## 6. Subvista `04 · ACL Legado y Auditoría` (↔ BPMN `04 · Consulta del historial`)
```mermaid
flowchart LR
  subgraph Agregado_HistorialVista["Agregado HistorialVista<br/><i>Agregado</i>"]
    s4_root_hist(("HistorialCanónico<br/><i>Raíz de Agregado</i>"))
    s4_rep_cache(("CacheTwoLevelRepo<br/><i>Repositorio</i>"))
    s4_srv_abac(("AutorizadorABAC<br/><i>Servicio de Dominio</i>"))
  end
  subgraph Agregado_TrazaAcceso["Agregado TrazaAcceso<br/><i>Agregado</i>"]
    s4_root_aud(("TrazaAccesoClínico<br/><i>Raíz de Agregado</i>"))
    s4_rep_worm(("S3ObjectLockRepo<br/><i>Repositorio</i>"))
  end
  s4_act_esp("Especialista<br/><i>Actor</i>")
  s4_cmd_cons(("ConsultarHistorial<br/><i>Comando</i>"))
  s4_ev_lect(("LecturaAuditada<br/><i>Evento</i>"))
  s4_pol_aud(("PolíticaTrazabilidad<br/><i>Política</i>"))
  s4_cmd_aud(("RegistrarTrazaWORM<br/><i>Comando</i>"))
  s4_ev_worm(("TrazaGrabadaWORM<br/><i>Evento</i>"))
  s4_act_aud("Auditor Clínico<br/><i>Actor</i>")
  s4_acl_soap["ACL SOAP Nocturno<br/><i>Capa Anticorrupción (ACL)</i>"]
  s4_ext_soap("Legado SOAP + BD RO<br/><i>Sistema Externo</i>")
  s4_act_esp -->|"ejecuta"| s4_cmd_cons
  s4_cmd_cons -->|"produce"| s4_ev_lect
  s4_cmd_cons -->|"consulta modelo"| s4_root_hist
  s4_root_hist -->|"lee caché TTL"| s4_rep_cache
  s4_root_hist -->|"exige ABAC"| s4_srv_abac
  s4_root_hist -->|"usa puerto ACL"| s4_acl_soap
  s4_acl_soap -->|"llama con CB"| s4_ext_soap
  s4_ev_lect -->|"activa"| s4_pol_aud
  s4_pol_aud -->|"emite"| s4_cmd_aud
  s4_cmd_aud -->|"produce"| s4_ev_worm
  s4_cmd_aud -->|"invoca agregado"| s4_root_aud
  s4_root_aud -->|"graba compliance"| s4_rep_worm
  s4_ev_worm -->|"reporta"| s4_act_aud
```
