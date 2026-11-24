package co.edu.ucc.redsalud.receta.aplicacion.puertos.salida;

import co.edu.ucc.redsalud.receta.dominio.RecetaMedica;
import java.util.Optional;

public interface RecetaRepository {

    Optional<RecetaMedica> buscarPorId(String recetaId);

    RecetaMedica guardar(RecetaMedica receta);
}
