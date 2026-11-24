package co.edu.ucc.redsalud.sincronizacion.infraestructura.salida.memoria;

import co.edu.ucc.redsalud.sincronizacion.aplicacion.puertos.salida.IdempotenciaRepository;
import co.edu.ucc.redsalud.sincronizacion.aplicacion.puertos.salida.OutboxRepository;
import co.edu.ucc.redsalud.sincronizacion.aplicacion.puertos.salida.PublicadorOutboxPort;
import co.edu.ucc.redsalud.sincronizacion.dominio.ConflictoLww;
import co.edu.ucc.redsalud.sincronizacion.dominio.MensajeOutbox;
import co.edu.ucc.redsalud.sincronizacion.dominio.ResultadoSincronizacion;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SincronizacionMemoriaAdapter implements IdempotenciaRepository, OutboxRepository, PublicadorOutboxPort {

    private final Map<String, ResultadoSincronizacion> claves = new ConcurrentHashMap<>();
    private final List<ConflictoLww> conflictos = new CopyOnWriteArrayList<>();
    private final Map<String, MensajeOutbox> outbox = new ConcurrentHashMap<>();
    private final List<MensajeOutbox> publicados = new CopyOnWriteArrayList<>();

    @Override
    public Optional<ResultadoSincronizacion> buscarPorClave(String key) { return Optional.ofNullable(claves.get(key)); }
    @Override
    public void guardarResultado(ResultadoSincronizacion r) { claves.putIfAbsent(r.idempotencyKey(), r); }
    @Override
    public void guardarConflicto(ConflictoLww c) { conflictos.add(c); }
    @Override
    public List<ConflictoLww> listarConflictos(String id) {
        return conflictos.stream().filter(c -> c.agregadoId().equals(id)).toList();
    }
    @Override
    public void guardar(MensajeOutbox e) { outbox.put(e.eventoId(), e); }
    @Override
    public List<MensajeOutbox> buscarPendientes() {
        return outbox.values().stream().filter(e -> !e.publicado()).toList();
    }
    @Override
    public void marcarPublicado(String id) {
        outbox.computeIfPresent(id, (k, e) -> new MensajeOutbox(
                e.eventoId(), e.agregadoId(), e.tipoEvento(), e.payload(), true, e.creadoEn()));
    }
    @Override
    public void publicarOrdenado(MensajeOutbox e) { publicados.add(e); }
    public List<MensajeOutbox> publicados() { return List.copyOf(publicados); }
}
