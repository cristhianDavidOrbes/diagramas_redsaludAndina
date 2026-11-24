package co.edu.ucc.redsalud.sincronizacion.aplicacion.puertos.entrada;

import co.edu.ucc.redsalud.sincronizacion.dominio.LoteSincronizacion;
import co.edu.ucc.redsalud.sincronizacion.dominio.ResultadoSincronizacion;

public interface SincronizarEpisodiosUseCase {

    ResultadoSincronizacion sincronizar(LoteSincronizacion lote);

    int drenarOutboxPendiente();
}
