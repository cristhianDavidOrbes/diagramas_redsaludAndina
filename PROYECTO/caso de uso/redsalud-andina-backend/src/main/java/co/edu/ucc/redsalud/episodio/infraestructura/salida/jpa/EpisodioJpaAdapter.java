package co.edu.ucc.redsalud.episodio.infraestructura.salida.jpa;

import co.edu.ucc.redsalud.episodio.aplicacion.puertos.salida.EpisodioRepository;
import co.edu.ucc.redsalud.episodio.dominio.AdjuntoCifrado;
import co.edu.ucc.redsalud.episodio.dominio.EpisodioClinico;
import java.util.Optional;

public class EpisodioJpaAdapter implements EpisodioRepository {

    private final EpisodioJpaRepository jpa;
    private final String kmsKeyId;
    private final String regionAws;

    public EpisodioJpaAdapter(EpisodioJpaRepository jpa, String kmsKeyId, String regionAws) {
        this.jpa = jpa;
        this.kmsKeyId = kmsKeyId;
        this.regionAws = regionAws;
    }

    @Override
    public Optional<EpisodioClinico> buscarPorId(String id) {
        return jpa.findById(id).map(this::aDominio);
    }

    @Override
    public EpisodioClinico guardar(EpisodioClinico ep) {
        String s3Key = ep.adjunto() != null ? ep.adjunto().s3Key() : null;
        EpisodioJpaEntity guardado = jpa.save(new EpisodioJpaEntity(
                ep.episodioId(), ep.pacienteId(), ep.centroId(), ep.notaEvolucion(),
                ep.signosVitales(), s3Key, ep.actualizadoEn(), ep.version()));
        return aDominio(guardado);
    }

    private EpisodioClinico aDominio(EpisodioJpaEntity e) {
        AdjuntoCifrado adj = e.getAdjuntoS3Key() != null
                ? new AdjuntoCifrado(e.getAdjuntoS3Key(), kmsKeyId, regionAws, false) : null;
        return new EpisodioClinico(e.getEpisodioId(), e.getPacienteId(), e.getCentroId(),
                e.getNotaEvolucion(), e.getSignosVitales(), adj, e.getActualizadoEn(), e.getVersion());
    }
}
