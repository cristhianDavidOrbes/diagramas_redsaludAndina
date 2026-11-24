package co.edu.ucc.redsalud.legado.infraestructura.salida.soap;

import co.edu.ucc.redsalud.legado.aplicacion.puertos.salida.HistorialClinicoPort;
import co.edu.ucc.redsalud.legado.dominio.HistorialCanonico;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.springframework.ws.client.core.WebServiceTemplate;

public class AdaptadorSoapResiliente implements HistorialClinicoPort {

    private final WebServiceTemplate wsTemplate;
    private final TraductorLegadoSoap traductor;
    private final String soapUri;

    public AdaptadorSoapResiliente(WebServiceTemplate wsTemplate, TraductorLegadoSoap traductor, String soapUri) {
        this.wsTemplate = wsTemplate;
        this.traductor = traductor;
        this.soapUri = soapUri;
    }

    @Override
    @CircuitBreaker(name = "legadoSoap", fallbackMethod = "fallbackSoap")
    public Optional<HistorialCanonico> consultarLegado(String pacienteId) {
        String xmlReq = "<ConsultaHistorial><COD_PAC_HIST>LEG-" + pacienteId + "</COD_PAC_HIST></ConsultaHistorial>";
        StringWriter out = new StringWriter();
        wsTemplate.sendSourceAndReceiveToResult(soapUri, new StreamSource(new StringReader(xmlReq)), new StreamResult(out));
        String xmlResp = out.toString();
        SoapHistorialDto dto = new SoapHistorialDto(
                extraer(xmlResp, "COD_PAC_HIST", "LEG-" + pacienteId),
                extraer(xmlResp, "XML_RESUMEN_ANT", "Antecedentes recuperados de legado"),
                extraer(xmlResp, "ADMIN_CODE", ""),
                extraer(xmlResp, "FEC_ULT_CARGA_NOCT", null));
        return Optional.of(traductor.aCanonico(dto));
    }

    public Optional<HistorialCanonico> fallbackSoap(String pacienteId, Throwable causa) {
        return Optional.empty();
    }

    private String extraer(String xml, String tag, String defecto) {
        String open = "<" + tag + ">";
        String close = "</" + tag + ">";
        int i = xml.indexOf(open);
        int j = xml.indexOf(close);
        return (i >= 0 && j > i) ? xml.substring(i + open.length(), j) : defecto;
    }
}
