package co.edu.ucc.redsalud.episodio.infraestructura.salida.memoria;

import co.edu.ucc.redsalud.episodio.aplicacion.puertos.salida.AdjuntoStoragePort;
import co.edu.ucc.redsalud.episodio.aplicacion.puertos.salida.EpisodioRepository;
import co.edu.ucc.redsalud.episodio.dominio.AdjuntoCifrado;
import co.edu.ucc.redsalud.episodio.dominio.EpisodioClinico;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class EpisodioMemoriaAdapter implements EpisodioRepository, AdjuntoStoragePort {

    private final Map<String, EpisodioClinico> episodios = new ConcurrentHashMap<>();
    private final String kmsKeyId;
    private final String regionAws;

    public EpisodioMemoriaAdapter(String kmsKeyId, String regionAws) {
        this.kmsKeyId = kmsKeyId;
        this.regionAws = regionAws;
    }

    @Override
    public Optional<EpisodioClinico> buscarPorId(String id) {
        return Optional.ofNullable(episodios.get(id));
    }

    @Override
    public EpisodioClinico guardar(EpisodioClinico episodio) {
        episodios.put(episodio.episodioId(), episodio);
        return episodio;
    }

    @Override
    public AdjuntoCifrado almacenarCifrado(String episodioId, String nombreArchivo, byte[] contenido) {
        return new AdjuntoCifrado("episodios/" + episodioId + "/" + nombreArchivo, kmsKeyId, regionAws, false);
    }
}
