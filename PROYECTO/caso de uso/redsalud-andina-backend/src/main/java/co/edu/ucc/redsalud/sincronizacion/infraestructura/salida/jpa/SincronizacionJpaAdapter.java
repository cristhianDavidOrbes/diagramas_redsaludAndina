package co.edu.ucc.redsalud.sincronizacion.infraestructura.salida.jpa;

import co.edu.ucc.redsalud.sincronizacion.aplicacion.puertos.salida.IdempotenciaRepository;
import co.edu.ucc.redsalud.sincronizacion.aplicacion.puertos.salida.OutboxRepository;
import co.edu.ucc.redsalud.sincronizacion.dominio.ConflictoLww;
import co.edu.ucc.redsalud.sincronizacion.dominio.MensajeOutbox;
import co.edu.ucc.redsalud.sincronizacion.dominio.ResultadoSincronizacion;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class SincronizacionJpaAdapter implements IdempotenciaRepository, OutboxRepository {

    private final ClaveIdempotenciaJpaRepository claves;
    private final OutboxEventoJpaRepository outbox;
    private final ConflictoLwwJpaRepository conflictos;

    public SincronizacionJpaAdapter(ClaveIdempotenciaJpaRepository claves, OutboxEventoJpaRepository outbox,
                                    ConflictoLwwJpaRepository conflictos) {
        this.claves = claves;
        this.outbox = outbox;
        this.conflictos = conflictos;
    }

    @Override
    public Optional<ResultadoSincronizacion> buscarPorClave(String key) {
        return claves.findById(key).map(e -> new ResultadoSincronizacion(
                e.getIdempotencyKey(), e.getAgregadoId(), e.getEstadoResultado(),
                e.getCodigoHttp(), e.getMensaje(), false, false));
    }

    @Override
    public void guardarResultado(ResultadoSincronizacion r) {
        claves.save(new ClaveIdempotenciaJpaEntity(
                r.idempotencyKey(), r.agregadoId(), r.estado(), r.codigoHttp(), r.mensaje(), Instant.now()));
    }

    @Override
    public void guardarConflicto(ConflictoLww c) {
        conflictos.save(new ConflictoLwwJpaEntity(
                c.conflictoId(), c.agregadoId(), c.campo(), c.valorGanador(), c.valorDesplazado(), c.registradoEn()));
    }

    @Override
    public List<ConflictoLww> listarConflictos(String agregadoId) {
        return conflictos.findByAgregadoId(agregadoId).stream().map(e -> new ConflictoLww(
                e.getConflictoId(), e.getAgregadoId(), e.getCampo(),
                e.getValorGanador(), e.getValorDesplazado(), e.getRegistradoEn())).toList();
    }

    @Override
    public void guardar(MensajeOutbox m) {
        outbox.save(new OutboxEventoJpaEntity(
                m.eventoId(), m.agregadoId(), m.tipoEvento(), m.payload(), m.publicado(), m.creadoEn()));
    }

    @Override
    public List<MensajeOutbox> buscarPendientes() {
        return outbox.findByPublicadoFalseOrderByCreadoEnAsc().stream().map(e -> new MensajeOutbox(
                e.getEventoId(), e.getAgregadoId(), e.getTipoEvento(), e.getPayload(), e.isPublicado(), e.getCreadoEn())).toList();
    }

    @Override
    public void marcarPublicado(String eventoId) {
        outbox.findById(eventoId).ifPresent(e -> { e.marcarPublicado(); outbox.save(e); });
    }
}
