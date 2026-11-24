package co.edu.ucc.redsalud.legado.infraestructura.salida.memoria;

import co.edu.ucc.redsalud.legado.aplicacion.puertos.salida.HistorialClinicoPort;
import co.edu.ucc.redsalud.legado.dominio.HistorialCanonico;
import co.edu.ucc.redsalud.legado.dominio.MarcaFrescura;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class LegadoMemoriaAdapter implements HistorialClinicoPort {

    private final Map<String, String> antecedentes = new ConcurrentHashMap<>(Map.of(
            "PAC-001", "Hipertensión controlada; alergia a penicilina"));
    private final AtomicBoolean soapCaido = new AtomicBoolean(false);

    @Override
    public Optional<HistorialCanonico> consultarLegado(String pacienteId) {
        if (soapCaido.get()) {
            return Optional.empty();
        }
        return Optional.ofNullable(antecedentes.get(pacienteId))
                .map(res -> new HistorialCanonico(pacienteId, res, Instant.now(), MarcaFrescura.LIVE, false));
    }

    public void simularCaidaSoap(boolean caido) {
        soapCaido.set(caido);
    }

    public void registrarAntecedente(String pacienteId, String resumen) {
        antecedentes.put(pacienteId, resumen);
    }
}
