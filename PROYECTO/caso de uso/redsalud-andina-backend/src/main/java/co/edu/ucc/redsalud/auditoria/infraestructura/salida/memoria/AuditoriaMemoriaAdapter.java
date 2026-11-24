package co.edu.ucc.redsalud.auditoria.infraestructura.salida.memoria;

import co.edu.ucc.redsalud.auditoria.aplicacion.puertos.salida.ArchivoWormPort;
import co.edu.ucc.redsalud.auditoria.aplicacion.puertos.salida.TrazaAuditoriaRepository;
import co.edu.ucc.redsalud.auditoria.dominio.TrazaAccesoClinico;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuditoriaMemoriaAdapter implements TrazaAuditoriaRepository, ArchivoWormPort {

    private final List<TrazaAccesoClinico> bitacora = new CopyOnWriteArrayList<>();
    private final List<TrazaAccesoClinico> archivoWorm = new CopyOnWriteArrayList<>();

    @Override
    public TrazaAccesoClinico guardarAppendOnly(TrazaAccesoClinico traza) {
        bitacora.add(traza);
        return traza;
    }

    @Override
    public Optional<String> ultimoHash() {
        return bitacora.isEmpty() ? Optional.empty() : Optional.of(bitacora.getLast().hashActual());
    }

    @Override
    public List<TrazaAccesoClinico> buscarPorRecurso(String recursoId) {
        return bitacora.stream().filter(t -> t.recursoId().equals(recursoId)).toList();
    }

    @Override
    public List<TrazaAccesoClinico> listarTodas() { return List.copyOf(bitacora); }

    @Override
    public void archivarCompliance(TrazaAccesoClinico traza) { archivoWorm.add(traza); }

    public List<TrazaAccesoClinico> archivoWorm() { return List.copyOf(archivoWorm); }
}
