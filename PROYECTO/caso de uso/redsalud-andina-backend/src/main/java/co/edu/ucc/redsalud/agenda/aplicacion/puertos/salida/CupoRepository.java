package co.edu.ucc.redsalud.agenda.aplicacion.puertos.salida;

import co.edu.ucc.redsalud.agenda.dominio.CupoEspecialista;
import java.util.Optional;

public interface CupoRepository {

    Optional<CupoEspecialista> buscarPorId(String cupoId);

    CupoEspecialista guardar(CupoEspecialista cupo);
}
