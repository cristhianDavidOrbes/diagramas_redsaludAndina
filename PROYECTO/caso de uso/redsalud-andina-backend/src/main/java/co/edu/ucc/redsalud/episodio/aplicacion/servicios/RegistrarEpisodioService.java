package co.edu.ucc.redsalud.episodio.aplicacion.servicios;

import co.edu.ucc.redsalud.episodio.aplicacion.puertos.entrada.RegistrarEpisodioUseCase;
import co.edu.ucc.redsalud.episodio.aplicacion.puertos.salida.AdjuntoStoragePort;
import co.edu.ucc.redsalud.episodio.aplicacion.puertos.salida.EpisodioRepository;
import co.edu.ucc.redsalud.episodio.dominio.AdjuntoCifrado;
import co.edu.ucc.redsalud.episodio.dominio.EpisodioClinico;
import co.edu.ucc.redsalud.episodio.dominio.EpisodioException;
import java.time.Instant;

public class RegistrarEpisodioService implements RegistrarEpisodioUseCase {

    private final EpisodioRepository repositorio;
    private final AdjuntoStoragePort storage;
    private final String regionPermitida;

    public RegistrarEpisodioService(EpisodioRepository repositorio, AdjuntoStoragePort storage, String regionPermitida) {
        this.repositorio = repositorio;
        this.storage = storage;
        this.regionPermitida = regionPermitida;
    }

    @Override
    public EpisodioClinico registrar(String episodioId, String pacienteId, String centroId,
                                     String notaEvolucion, String signosVitales) {
        EpisodioClinico episodio = new EpisodioClinico(
                episodioId, pacienteId, centroId, notaEvolucion, signosVitales, null, Instant.now(), 0L);
        return repositorio.guardar(episodio);
    }

    @Override
    public EpisodioClinico adjuntarExamen(String episodioId, String nombreArchivo, byte[] contenido) {
        EpisodioClinico episodio = repositorio.buscarPorId(episodioId)
                .orElseThrow(() -> new EpisodioException("Episodio no encontrado: " + episodioId));
        AdjuntoCifrado cifrado = storage.almacenarCifrado(episodioId, nombreArchivo, contenido);
        episodio.adjuntar(cifrado, regionPermitida);
        return repositorio.guardar(episodio);
    }
}
