package co.edu.ucc.redsalud.legado.aplicacion.puertos.salida;

import co.edu.ucc.redsalud.legado.dominio.HistorialCanonico;
import java.util.Optional;

public interface ReadModelCachePort {

    Optional<HistorialCanonico> buscar(String pacienteId);

    void guardar(HistorialCanonico historial);
}
