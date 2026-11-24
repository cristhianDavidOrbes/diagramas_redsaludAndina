package co.edu.ucc.redsalud.legado.aplicacion.servicios;

import co.edu.ucc.redsalud.legado.aplicacion.puertos.entrada.ConsultarHistorialUseCase;
import co.edu.ucc.redsalud.legado.aplicacion.puertos.salida.HistorialClinicoPort;
import co.edu.ucc.redsalud.legado.aplicacion.puertos.salida.ReadModelCachePort;
import co.edu.ucc.redsalud.legado.dominio.HistorialCanonico;
import co.edu.ucc.redsalud.legado.dominio.LegadoException;
import co.edu.ucc.redsalud.legado.dominio.MarcaFrescura;
import java.time.Instant;
import java.util.Optional;

public class ConsultarHistorialService implements ConsultarHistorialUseCase {

    private final ReadModelCachePort cache;
    private final HistorialClinicoPort legadoPort;
    private final long ttlSegundos;

    public ConsultarHistorialService(ReadModelCachePort cache, HistorialClinicoPort legadoPort, long ttlSegundos) {
        this.cache = cache;
        this.legadoPort = legadoPort;
        this.ttlSegundos = ttlSegundos;
    }

    @Override
    public HistorialCanonico consultar(String pacienteId, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new LegadoException("Motivo clínico obligatorio para consultar historial");
        }
        Optional<HistorialCanonico> enCache = cache.buscar(pacienteId);
        if (enCache.isPresent() && enCache.get().estaVigente(Instant.now(), ttlSegundos)) {
            return enCache.get().conFrescura(MarcaFrescura.FRESH, false);
        }
        Optional<HistorialCanonico> desdeLegado = consultarSeguro(pacienteId);
        if (desdeLegado.isPresent()) {
            HistorialCanonico vivo = desdeLegado.get().conFrescura(MarcaFrescura.LIVE, false);
            cache.guardar(vivo);
            return vivo;
        }
        return enCache
                .map(stale -> stale.conFrescura(MarcaFrescura.STALE, true))
                .orElseThrow(() -> new LegadoException("Historial no disponible para paciente: " + pacienteId));
    }

    private Optional<HistorialCanonico> consultarSeguro(String pacienteId) {
        try {
            return legadoPort.consultarLegado(pacienteId);
        } catch (LegadoException ex) {
            return Optional.empty();
        }
    }
}
