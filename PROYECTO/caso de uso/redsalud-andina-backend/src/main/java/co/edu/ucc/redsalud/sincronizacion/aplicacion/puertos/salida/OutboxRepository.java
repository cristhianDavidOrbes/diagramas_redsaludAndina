package co.edu.ucc.redsalud.sincronizacion.aplicacion.puertos.salida;

import co.edu.ucc.redsalud.sincronizacion.dominio.MensajeOutbox;
import java.util.List;

public interface OutboxRepository {

    void guardar(MensajeOutbox evento);

    List<MensajeOutbox> buscarPendientes();

    void marcarPublicado(String eventoId);
}
