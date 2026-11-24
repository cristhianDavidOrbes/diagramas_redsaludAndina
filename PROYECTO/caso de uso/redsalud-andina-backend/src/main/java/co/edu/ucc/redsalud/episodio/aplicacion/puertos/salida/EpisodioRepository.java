package co.edu.ucc.redsalud.episodio.aplicacion.puertos.salida;

import co.edu.ucc.redsalud.episodio.dominio.EpisodioClinico;
import java.util.Optional;

public interface EpisodioRepository {

    Optional<EpisodioClinico> buscarPorId(String episodioId);

    EpisodioClinico guardar(EpisodioClinico episodio);
}
