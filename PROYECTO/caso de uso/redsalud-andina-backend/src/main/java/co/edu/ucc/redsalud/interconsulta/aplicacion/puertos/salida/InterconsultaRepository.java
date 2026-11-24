package co.edu.ucc.redsalud.interconsulta.aplicacion.puertos.salida;

import co.edu.ucc.redsalud.interconsulta.dominio.Interconsulta;
import java.util.Optional;

public interface InterconsultaRepository {

    Optional<Interconsulta> buscarPorId(String interconsultaId);

    Interconsulta guardar(Interconsulta interconsulta);
}
