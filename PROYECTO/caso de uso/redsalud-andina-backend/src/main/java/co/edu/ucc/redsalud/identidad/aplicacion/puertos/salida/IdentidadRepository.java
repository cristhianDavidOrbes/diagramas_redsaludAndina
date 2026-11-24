package co.edu.ucc.redsalud.identidad.aplicacion.puertos.salida;

import co.edu.ucc.redsalud.identidad.dominio.PrincipalClinico;
import java.util.Optional;

public interface IdentidadRepository {

    Optional<PrincipalClinico> buscarPorId(String usuarioId);

    PrincipalClinico guardar(PrincipalClinico principal);
}
