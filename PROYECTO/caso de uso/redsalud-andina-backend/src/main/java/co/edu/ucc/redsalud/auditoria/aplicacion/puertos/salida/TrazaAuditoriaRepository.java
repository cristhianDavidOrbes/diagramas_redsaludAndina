package co.edu.ucc.redsalud.auditoria.aplicacion.puertos.salida;

import co.edu.ucc.redsalud.auditoria.dominio.TrazaAccesoClinico;
import java.util.List;
import java.util.Optional;

public interface TrazaAuditoriaRepository {

    TrazaAccesoClinico guardarAppendOnly(TrazaAccesoClinico traza);

    Optional<String> ultimoHash();

    List<TrazaAccesoClinico> buscarPorRecurso(String recursoId);

    List<TrazaAccesoClinico> listarTodas();
}
