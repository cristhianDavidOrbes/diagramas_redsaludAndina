package co.edu.ucc.redsalud.legado.infraestructura.salida.soap;

import co.edu.ucc.redsalud.legado.dominio.HistorialCanonico;
import co.edu.ucc.redsalud.legado.dominio.LegadoException;
import co.edu.ucc.redsalud.legado.dominio.MarcaFrescura;
import java.time.Instant;
import java.time.format.DateTimeParseException;

public class TraductorLegadoSoap {

    public HistorialCanonico aCanonico(SoapHistorialDto dto) {
        if (dto == null || dto.codPacHist() == null || dto.codPacHist().isBlank()) {
            throw new LegadoException("Respuesta SOAP sin COD_PAC_HIST válido");
        }
        String pacienteId = dto.codPacHist().replace("LEG-", "");
        String resumen = dto.xmlResumenAnt() == null ? "Sin antecedentes" : dto.xmlResumenAnt().trim();
        Instant fecha = parsearFecha(dto.fecUltCargaNoct());
        return new HistorialCanonico(pacienteId, resumen, fecha, MarcaFrescura.LIVE, false);
    }

    private Instant parsearFecha(String fecUltCargaNoct) {
        try {
            return fecUltCargaNoct != null ? Instant.parse(fecUltCargaNoct) : Instant.now();
        } catch (DateTimeParseException ex) {
            return Instant.now();
        }
    }
}
