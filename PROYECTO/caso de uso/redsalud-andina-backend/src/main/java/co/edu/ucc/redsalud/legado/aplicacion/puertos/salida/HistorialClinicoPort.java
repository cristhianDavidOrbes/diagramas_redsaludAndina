package co.edu.ucc.redsalud.legado.aplicacion.puertos.salida;

import co.edu.ucc.redsalud.legado.dominio.HistorialCanonico;
import java.util.Optional;

public interface HistorialClinicoPort {

    Optional<HistorialCanonico> consultarLegado(String pacienteId);
}
