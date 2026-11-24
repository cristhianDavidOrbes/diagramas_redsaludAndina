package co.edu.ucc.redsalud;

import co.edu.ucc.redsalud.legado.aplicacion.puertos.salida.ReadModelCachePort;
import co.edu.ucc.redsalud.legado.dominio.HistorialCanonico;
import co.edu.ucc.redsalud.legado.dominio.LegadoException;
import co.edu.ucc.redsalud.legado.dominio.MarcaFrescura;
import co.edu.ucc.redsalud.legado.infraestructura.salida.memoria.LegadoMemoriaAdapter;
import co.edu.ucc.redsalud.legado.infraestructura.salida.soap.AdaptadorSoapResiliente;
import co.edu.ucc.redsalud.legado.infraestructura.salida.soap.SoapHistorialDto;
import co.edu.ucc.redsalud.legado.infraestructura.salida.soap.TraductorLegadoSoap;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.test.client.MockWebServiceServer;
import org.springframework.xml.transform.StringSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.ws.test.client.RequestMatchers.anything;
import static org.springframework.ws.test.client.ResponseCreators.withPayload;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("esqueleto")
class LegadoAclYFallbackTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReadModelCachePort cache;

    @Autowired
    private LegadoMemoriaAdapter legadoMemoria;

    @Test
    void contratoTraduccionSoapACanonicoSinFiltrarModeloLegado() {
        TraductorLegadoSoap traductor = new TraductorLegadoSoap();
        SoapHistorialDto dto = new SoapHistorialDto(
                "LEG-PAC-777", "  Asma leve intermitente  ", "ADMIN-SECRETO-99", "2026-11-24T03:00:00Z");

        HistorialCanonico canonico = traductor.aCanonico(dto);

        assertThat(canonico.pacienteId()).isEqualTo("PAC-777");
        assertThat(canonico.resumenAntecedentes()).isEqualTo("Asma leve intermitente");
        assertThat(canonico.frescura()).isEqualTo(MarcaFrescura.LIVE);
        assertThat(canonico.cabeceraFrescura()).startsWith("live; synced-at=2026-11-24T03:00:00Z");
        assertThatThrownBy(() -> traductor.aCanonico(new SoapHistorialDto(" ", "", "", null)))
                .isInstanceOf(LegadoException.class);
    }

    @Test
    void adaptadorSoapTraduceRespuestaXmlConSpringWsTest() {
        WebServiceTemplate ws = new WebServiceTemplate();
        MockWebServiceServer mockSoap = MockWebServiceServer.createServer(ws);
        mockSoap.expect(anything()).andRespond(withPayload(new StringSource("""
                <RespuestaHistorial>
                    <COD_PAC_HIST>LEG-PAC-500</COD_PAC_HIST>
                    <XML_RESUMEN_ANT>Diabetes tipo 2 en control</XML_RESUMEN_ANT>
                    <ADMIN_CODE>IGNORADO</ADMIN_CODE>
                    <FEC_ULT_CARGA_NOCT>2026-11-24T02:00:00Z</FEC_ULT_CARGA_NOCT>
                </RespuestaHistorial>""")));

        AdaptadorSoapResiliente adaptador = new AdaptadorSoapResiliente(ws, new TraductorLegadoSoap(), "http://mock/ws");
        Optional<HistorialCanonico> res = adaptador.consultarLegado("PAC-500");

        assertThat(res).isPresent();
        assertThat(res.get().pacienteId()).isEqualTo("PAC-500");
        assertThat(res.get().resumenAntecedentes()).isEqualTo("Diabetes tipo 2 en control");
        mockSoap.verify();
    }

    @Test
    void ejercicioDetonanteSoapApagadoRespondeReadModelStaleEnMenosDe6sSin500() throws Exception {
        // Sembramos caché con copia vencida (> TTL 300s) y apagamos el legado SOAP
        HistorialCanonico copiaVencida = new HistorialCanonico(
                "PAC-FALLBACK", "Hipertensión; control en centro rural",
                Instant.now().minusSeconds(900), MarcaFrescura.FRESH, false);
        cache.guardar(copiaVencida);
        legadoMemoria.simularCaidaSoap(true);
        try {
            long inicio = System.currentTimeMillis();
            mockMvc.perform(get("/historial/PAC-FALLBACK").param("motivo", "teleconsulta"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("X-Data-Freshness", startsWith("stale; synced-at=")))
                    .andExpect(jsonPath("$.degradadoDesdeCache").value(true))
                    .andExpect(jsonPath("$.resumenAntecedentes").value("Hipertensión; control en centro rural"));
            long duracionMs = System.currentTimeMillis() - inicio;
            assertThat(duracionMs).isLessThan(6_000L);
        } finally {
            legadoMemoria.simularCaidaSoap(false);
        }
    }

    @Test
    void cacheVigenteRespondeFreshEnMenosDe2s() throws Exception {
        HistorialCanonico vigente = new HistorialCanonico(
                "PAC-FRESH", "Alergia a AINES", Instant.now(), MarcaFrescura.LIVE, false);
        cache.guardar(vigente);

        long inicio = System.currentTimeMillis();
        mockMvc.perform(get("/historial/PAC-FRESH").param("motivo", "control"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Data-Freshness", startsWith("fresh; synced-at=")))
                .andExpect(jsonPath("$.degradadoDesdeCache").value(false));
        assertThat(System.currentTimeMillis() - inicio).isLessThan(2_000L);
    }
}
