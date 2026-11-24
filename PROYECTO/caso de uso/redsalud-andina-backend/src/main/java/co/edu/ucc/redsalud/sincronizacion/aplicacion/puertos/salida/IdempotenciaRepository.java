package co.edu.ucc.redsalud.sincronizacion.aplicacion.puertos.salida;

import co.edu.ucc.redsalud.sincronizacion.dominio.ConflictoLww;
import co.edu.ucc.redsalud.sincronizacion.dominio.ResultadoSincronizacion;
import java.util.List;
import java.util.Optional;

public interface IdempotenciaRepository {

    Optional<ResultadoSincronizacion> buscarPorClave(String idempotencyKey);

    void guardarResultado(ResultadoSincronizacion resultado);

    void guardarConflicto(ConflictoLww conflicto);

    List<ConflictoLww> listarConflictos(String agregadoId);
}
