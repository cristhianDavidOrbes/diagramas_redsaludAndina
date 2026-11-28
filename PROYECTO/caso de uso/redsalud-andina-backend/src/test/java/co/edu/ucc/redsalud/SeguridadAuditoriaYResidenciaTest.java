package co.edu.ucc.redsalud;

import co.edu.ucc.redsalud.auditoria.aplicacion.puertos.entrada.AuditarAccesoUseCase;
import co.edu.ucc.redsalud.auditoria.aplicacion.servicios.AuditarAccesoService;
import co.edu.ucc.redsalud.auditoria.dominio.ReporteAuditoria;
import co.edu.ucc.redsalud.auditoria.dominio.TrazaAccesoClinico;
import co.edu.ucc.redsalud.auditoria.infraestructura.salida.memoria.AuditoriaMemoriaAdapter;
import co.edu.ucc.redsalud.episodio.infraestructura.salida.aws.S3AdjuntoKmsAdapter;
import co.edu.ucc.redsalud.identidad.aplicacion.puertos.salida.RegistroDenegacionPort;
import co.edu.ucc.redsalud.identidad.dominio.PrincipalClinico;
import co.edu.ucc.redsalud.identidad.dominio.RolUsuario;
import co.edu.ucc.redsalud.identidad.infraestructura.config.PoliticaAbacGate;
import co.edu.ucc.redsalud.identidad.infraestructura.salida.oidc.TraductorJwtOidc;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("esqueleto")
class SeguridadAuditoriaYResidenciaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PoliticaAbacGate abacGate;

    @Autowired
    private RegistroDenegacionPort registroDenegaciones;

    @Autowired
    private AuditarAccesoUseCase auditoria;

    @Test
    @WithMockUser(username = "ADMIN-1")
    void usuarioAdministrativoRecibe403YDenegacionQuedaAuditadaEnCapaServicio() throws Exception {
        int denegacionesPrevias = registroDenegaciones.listarDenegaciones().size();

        // 1) @PreAuthorize en capa de servicio bloquea rol ADMIN
        assertThatThrownBy(() -> abacGate.autorizarEnCapaServicio(
                "ADMIN-1", "PAC-001", "CENTRO-01", "HIST-PAC-001", "curiosidad"))
                .isInstanceOf(AccessDeniedException.class);

        // 2) AuthorizationManager en SecurityFilterChain responde 403 HTTP
        mockMvc.perform(get("/api/v1/identidad/protegido/HIST-PAC-001")
                        .header("X-Usuario-Id", "ADMIN-1")
                        .header("X-Paciente-Id", "PAC-001")
                        .header("X-Centro-Id", "CENTRO-01")
                        .header("X-Motivo", "consulta"))
                .andExpect(status().isForbidden());

        // 3) Médico rural con relación vigente y motivo clínico recibe 200 OK
        mockMvc.perform(get("/api/v1/identidad/protegido/HIST-PAC-001")
                        .header("X-Usuario-Id", "MED-RURAL-1")
                        .header("X-Paciente-Id", "PAC-001")
                        .header("X-Centro-Id", "CENTRO-01")
                        .header("X-Motivo", "teleconsulta"))
                .andExpect(status().isOk());

        assertThat(registroDenegaciones.listarDenegaciones().size()).isGreaterThanOrEqualTo(denegacionesPrevias + 2);
    }

    @Test
    void aclOidcTraduceClaimsJwtAPrincipalClinico() {
        Jwt jwt = new Jwt("token-demo", Instant.now(), Instant.now().plusSeconds(300),
                Map.of("alg", "RS256"),
                Map.of("sub", "ESP-9", "rol", "ESPECIALISTA", "centro_id", "CENTRO-05",
                        "especialidad", "PEDIATRIA", "pacientes", List.of("PAC-10")));

        PrincipalClinico principal = new TraductorJwtOidc().desdeJwt(jwt);
        assertThat(principal.usuarioId()).isEqualTo("ESP-9");
        assertThat(principal.rol()).isEqualTo(RolUsuario.ESPECIALISTA);
        assertThat(principal.pacientesAsignados()).contains("PAC-10");
    }

    @Test
    void emiteTokenJwtRs256YValidaBearerConAbac() throws Exception {
        String respMed = mockMvc.perform(post("/api/v1/identidad/token")
                        .contentType("application/json")
                        .content("{\"usuarioId\":\"MED-RURAL-1\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String jwtMed = respMed.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
        assertThat(jwtMed.split("\\.")).hasSize(3);

        mockMvc.perform(get("/api/v1/identidad/protegido/HIST-PAC-001")
                        .header("Authorization", "Bearer " + jwtMed)
                        .header("X-Paciente-Id", "PAC-001")
                        .header("X-Centro-Id", "CENTRO-01")
                        .header("X-Motivo", "teleconsulta"))
                .andExpect(status().isOk());

        // Token alterado falla la verificación criptográfica RS256 -> 403
        mockMvc.perform(get("/api/v1/identidad/protegido/HIST-PAC-001")
                        .header("Authorization", "Bearer " + jwtMed + "X")
                        .header("X-Paciente-Id", "PAC-001")
                        .header("X-Centro-Id", "CENTRO-01")
                        .header("X-Motivo", "teleconsulta"))
                .andExpect(status().isForbidden());

        String respAdmin = mockMvc.perform(post("/api/v1/identidad/token")
                        .contentType("application/json")
                        .content("{\"usuarioId\":\"ADMIN-1\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String jwtAdmin = respAdmin.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

        // Token válido de ADMIN-1 pero sin rol clínico -> 403 ABAC
        mockMvc.perform(get("/api/v1/identidad/protegido/HIST-PAC-001")
                        .header("Authorization", "Bearer " + jwtAdmin)
                        .header("X-Paciente-Id", "PAC-001")
                        .header("X-Centro-Id", "CENTRO-01")
                        .header("X-Motivo", "consulta"))
                .andExpect(status().isForbidden());

        // Endpoint real corto: /auth/token + GET /historial/PAC-001 con Bearer sin headers extra
        mockMvc.perform(get("/historial/PAC-001").header("Authorization", "Bearer " + jwtMed))
                .andExpect(status().isOk());
        mockMvc.perform(get("/historial/PAC-001").header("Authorization", "Bearer " + jwtAdmin))
                .andExpect(status().isForbidden());
    }

    @Test
    void politicaBucketS3ExigeKmsYBloqueaReplicacionCrossRegion() {
        S3AdjuntoKmsAdapter adapter = new S3AdjuntoKmsAdapter(null, "redsalud-adjuntos-co", "alias/kms-co", "us-east-1");
        String policy = adapter.politicaBucketRegionUnicaJson();

        assertThat(policy).contains("s3:PutBucketReplication");
        assertThat(policy).contains("\"Effect\":\"Deny\"");
        assertThat(policy).contains("aws:kms");
    }

    @Test
    void generaReporteReproducibleQuienVioDocumentoYDetectaAlteracionCadenaSha256() throws Exception {
        auditoria.registrar("MED-RURAL-1", "MEDICO_RURAL", "HIST-PAC-001", "teleconsulta programada", true, "CENTRO-01");
        auditoria.registrar("ESP-CARD-1", "ESPECIALISTA", "HIST-PAC-001", "interconsulta cardiología", true, "CENTRO-02");
        auditoria.registrar("ADMIN-1", "ADMIN", "HIST-PAC-001", "acceso sin rol clínico", false, "CENTRO-01");

        ReporteAuditoria reporte = auditoria.generarReporte("HIST-PAC-001");
        String md = reporte.aMarkdown();

        assertThat(reporte.cadenaIntegra()).isTrue();
        assertThat(reporte.accesosPermitidos()).isGreaterThanOrEqualTo(2);
        assertThat(reporte.accesosDenegados()).isGreaterThanOrEqualTo(1);
        assertThat(md).contains("MED-RURAL-1", "ESP-CARD-1", "ADMIN-1", "DENEGADO", "VERIFICADA");

        Files.writeString(Path.of("docs/reporte-auditoria-ejemplo.md"), md);

        // Prueba de integridad (ADR-004): alterar una traza rompe la cadena 100% de las veces
        List<TrazaAccesoClinico> alterable = new ArrayList<>();
        AuditoriaMemoriaAdapter worm = new AuditoriaMemoriaAdapter();
        AuditarAccesoService aislado = new AuditarAccesoService(new co.edu.ucc.redsalud.auditoria.aplicacion.puertos.salida.TrazaAuditoriaRepository() {
            @Override public TrazaAccesoClinico guardarAppendOnly(TrazaAccesoClinico t) { alterable.add(t); return t; }
            @Override public Optional<String> ultimoHash() { return alterable.isEmpty() ? Optional.empty() : Optional.of(alterable.getLast().hashActual()); }
            @Override public List<TrazaAccesoClinico> buscarPorRecurso(String r) { return alterable; }
            @Override public List<TrazaAccesoClinico> listarTodas() { return alterable; }
        }, worm);
        aislado.registrar("MED-1", "MEDICO_RURAL", "DOC-9", "control", true, "C-1");
        assertThat(aislado.verificarIntegridadCadena()).isTrue();

        TrazaAccesoClinico original = alterable.getFirst();
        alterable.set(0, new TrazaAccesoClinico(original.trazaId(), original.usuarioId(), original.rol(),
                original.recursoId(), "MOTIVO-ALTERADO", original.permitido(), original.centroId(),
                original.ocurridoEn(), original.hashAnterior(), original.hashActual()));
        assertThat(aislado.verificarIntegridadCadena()).isFalse();
    }
}
