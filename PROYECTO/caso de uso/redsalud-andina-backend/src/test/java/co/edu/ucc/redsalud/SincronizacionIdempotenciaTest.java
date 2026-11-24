package co.edu.ucc.redsalud;

import co.edu.ucc.redsalud.sincronizacion.aplicacion.servicios.SincronizarEpisodiosService;
import co.edu.ucc.redsalud.sincronizacion.dominio.ClaveIdempotencia;
import co.edu.ucc.redsalud.sincronizacion.dominio.ConflictoLww;
import co.edu.ucc.redsalud.sincronizacion.dominio.LoteSincronizacion;
import co.edu.ucc.redsalud.sincronizacion.dominio.ResultadoSincronizacion;
import co.edu.ucc.redsalud.sincronizacion.dominio.SincronizacionException;
import co.edu.ucc.redsalud.sincronizacion.dominio.TipoOperacionOffline;
import co.edu.ucc.redsalud.sincronizacion.infraestructura.salida.memoria.SincronizacionMemoriaAdapter;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("esqueleto")
class SincronizacionIdempotenciaTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void tripleEnvioConOrdenAlteradoYReinicioDeServicioProduceCeroDuplicadosYCeroPerdida() {
        // El almacén simula persistencia durable (BD) que sobrevive al reinicio del servicio
        SincronizacionMemoriaAdapter almacenPersistente = new SincronizacionMemoriaAdapter();
        SincronizarEpisodiosService instancia1 = new SincronizarEpisodiosService(
                almacenPersistente, almacenPersistente, almacenPersistente);

        LoteSincronizacion lote1 = new LoteSincronizacion(
                new ClaveIdempotencia("ULID-LOTE-001"), "EP-42", TipoOperacionOffline.NOTA_EVOLUCION,
                "notaEvolucion", "Paciente estable", Instant.parse("2026-11-24T10:00:00Z"), null, null);
        LoteSincronizacion lote2 = new LoteSincronizacion(
                new ClaveIdempotencia("ULID-LOTE-002"), "EP-42", TipoOperacionOffline.SIGNOS_VITALES,
                "signosVitales", "TA 118/76", Instant.parse("2026-11-24T10:01:00Z"), null, null);

        // Envío 1: L1, L2 -> 201 Created
        assertThat(instancia1.sincronizar(lote1).codigoHttp()).isEqualTo(201);
        assertThat(instancia1.sincronizar(lote2).codigoHttp()).isEqualTo(201);

        // Envío 2 con orden alterado (L2, L1) -> 200 OK idempotente
        ResultadoSincronizacion reintento2 = instancia1.sincronizar(lote2);
        assertThat(reintento2.codigoHttp()).isEqualTo(200);
        assertThat(reintento2.duplicado()).isTrue();

        // Reinicio simulado: nueva instancia del servicio sobre el mismo almacén persistente
        SincronizarEpisodiosService instancia2TrasReinicio = new SincronizarEpisodiosService(
                almacenPersistente, almacenPersistente, almacenPersistente);
        ResultadoSincronizacion reintento3 = instancia2TrasReinicio.sincronizar(lote1);
        assertThat(reintento3.codigoHttp()).isEqualTo(200);
        assertThat(reintento3.duplicado()).isTrue();

        // Drenado outbox: exactamente 2 eventos (0 duplicados, 0 pérdida)
        int publicados = instancia2TrasReinicio.drenarOutboxPendiente();
        assertThat(publicados).isEqualTo(2);
        assertThat(almacenPersistente.publicados()).hasSize(2);
        assertThat(instancia2TrasReinicio.drenarOutboxPendiente()).isZero();
    }

    @Test
    void conflictoAsistencialLwwRegistraAmbosValoresYRechazaCupoYRecetaEnBorde() {
        SincronizacionMemoriaAdapter repo = new SincronizacionMemoriaAdapter();
        SincronizarEpisodiosService service = new SincronizarEpisodiosService(repo, repo, repo);

        // LWW asistencial: llegan dos notas con sellos distintos -> ambos valores auditados
        LoteSincronizacion conflictoNota = new LoteSincronizacion(
                new ClaveIdempotencia("ULID-LWW-01"), "EP-99", TipoOperacionOffline.NOTA_EVOLUCION,
                "notaEvolucion", "Fiebre 38.5C", Instant.parse("2026-11-24T11:05:00Z"),
                "Afebril 36.8C", Instant.parse("2026-11-24T11:00:00Z"));
        ResultadoSincronizacion resLww = service.sincronizar(conflictoNota);

        assertThat(resLww.codigoHttp()).isEqualTo(201);
        assertThat(resLww.conflictoAuditado()).isTrue();
        List<ConflictoLww> auditados = repo.listarConflictos("EP-99");
        assertThat(auditados).hasSize(1);
        assertThat(auditados.getFirst().valorGanador()).isEqualTo("Fiebre 38.5C");
        assertThat(auditados.getFirst().valorDesplazado()).isEqualTo("Afebril 36.8C");

        // Cupo y receta firmada NO se resuelven en el borde -> 409 Conflict
        LoteSincronizacion cupoOffline = new LoteSincronizacion(
                new ClaveIdempotencia("ULID-CUPO-01"), "CUPO-9", TipoOperacionOffline.RESERVA_CUPO,
                "reserva", "PAC-1", Instant.now(), null, null);
        LoteSincronizacion recetaOffline = new LoteSincronizacion(
                new ClaveIdempotencia("ULID-REC-01"), "REC-9", TipoOperacionOffline.RECETA_FIRMADA,
                "receta", "Amoxicilina", Instant.now(), null, null);

        assertThat(service.sincronizar(cupoOffline).codigoHttp()).isEqualTo(409);
        assertThat(service.sincronizar(recetaOffline).codigoHttp()).isEqualTo(409);
        assertThatThrownBy(() -> new ClaveIdempotencia(" "))
                .isInstanceOf(SincronizacionException.class);
    }

    @Test
    void endpointPostSyncEpisodiosResponde201Luego200EnReintento() throws Exception {
        String body = """
                {"agregadoId":"EP-HTTP-1","tipo":"NOTA_EVOLUCION","campo":"notaEvolucion",
                 "valorEntrante":"Control rural","selloTiempoCentro":"2026-11-24T12:00:00Z"}""";

        mockMvc.perform(post("/sync/episodios")
                        .header("Idempotency-Key", "ULID-HTTP-100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.duplicado").value(false));

        mockMvc.perform(post("/sync/episodios")
                        .header("Idempotency-Key", "ULID-HTTP-100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duplicado").value(true));
    }
}
