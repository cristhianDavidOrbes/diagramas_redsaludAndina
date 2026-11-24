package co.edu.ucc.redsalud.sincronizacion.aplicacion.puertos.salida;

import co.edu.ucc.redsalud.sincronizacion.dominio.MensajeOutbox;

public interface PublicadorOutboxPort {

    void publicarOrdenado(MensajeOutbox evento);
}
