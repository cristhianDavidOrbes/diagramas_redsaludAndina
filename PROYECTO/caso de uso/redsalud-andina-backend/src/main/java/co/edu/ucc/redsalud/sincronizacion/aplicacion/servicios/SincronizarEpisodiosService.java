package co.edu.ucc.redsalud.sincronizacion.aplicacion.servicios;

import co.edu.ucc.redsalud.sincronizacion.aplicacion.puertos.entrada.SincronizarEpisodiosUseCase;
import co.edu.ucc.redsalud.sincronizacion.aplicacion.puertos.salida.IdempotenciaRepository;
import co.edu.ucc.redsalud.sincronizacion.aplicacion.puertos.salida.OutboxRepository;
import co.edu.ucc.redsalud.sincronizacion.aplicacion.puertos.salida.PublicadorOutboxPort;
import co.edu.ucc.redsalud.sincronizacion.dominio.LoteSincronizacion;
import co.edu.ucc.redsalud.sincronizacion.dominio.MensajeOutbox;
import co.edu.ucc.redsalud.sincronizacion.dominio.ResolutorConflictos;
import co.edu.ucc.redsalud.sincronizacion.dominio.ResultadoSincronizacion;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SincronizarEpisodiosService implements SincronizarEpisodiosUseCase {

    private final IdempotenciaRepository idempotencia;
    private final OutboxRepository outbox;
    private final PublicadorOutboxPort publicador;
    private final ResolutorConflictos resolutor = new ResolutorConflictos();

    public SincronizarEpisodiosService(IdempotenciaRepository idempotencia, OutboxRepository outbox,
                                       PublicadorOutboxPort publicador) {
        this.idempotencia = idempotencia;
        this.outbox = outbox;
        this.publicador = publicador;
    }

    @Override
    public ResultadoSincronizacion sincronizar(LoteSincronizacion lote) {
        Optional<ResultadoSincronizacion> previo = idempotencia.buscarPorClave(lote.clave().valor());
        if (previo.isPresent()) {
            return previo.get().comoReintentoIdempotente();
        }
        ResolutorConflictos.Evaluacion ev = resolutor.evaluar(lote);
        ResultadoSincronizacion resultado;
        if (ev.rechazado()) {
            resultado = new ResultadoSincronizacion(lote.clave().valor(), lote.agregadoId(),
                    "RECHAZADO_BORDE", 409, "Requiere consistencia fuerte en servidor: " + lote.tipo(), false, false);
        } else {
            ev.conflicto().ifPresent(idempotencia::guardarConflicto);
            resultado = new ResultadoSincronizacion(lote.clave().valor(), lote.agregadoId(),
                    "SINCRONIZADO", 201, "Aplicado: " + ev.valorAplicado(), false, ev.conflicto().isPresent());
            outbox.guardar(new MensajeOutbox(UUID.randomUUID().toString(), lote.agregadoId(),
                    "EpisodioSincronizado", lote.campo() + "=" + ev.valorAplicado(), false, Instant.now()));
        }
        idempotencia.guardarResultado(resultado);
        return resultado;
    }

    @Override
    public int drenarOutboxPendiente() {
        List<MensajeOutbox> pendientes = outbox.buscarPendientes();
        for (MensajeOutbox evento : pendientes) {
            publicador.publicarOrdenado(evento);
            outbox.marcarPublicado(evento.eventoId());
        }
        return pendientes.size();
    }
}
