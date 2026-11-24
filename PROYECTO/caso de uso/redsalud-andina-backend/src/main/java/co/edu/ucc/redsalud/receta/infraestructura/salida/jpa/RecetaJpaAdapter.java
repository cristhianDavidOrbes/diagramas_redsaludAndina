package co.edu.ucc.redsalud.receta.infraestructura.salida.jpa;

import co.edu.ucc.redsalud.receta.aplicacion.puertos.salida.RecetaRepository;
import co.edu.ucc.redsalud.receta.dominio.FirmaKms;
import co.edu.ucc.redsalud.receta.dominio.RecetaMedica;
import java.util.Optional;

public class RecetaJpaAdapter implements RecetaRepository {

    private final RecetaJpaRepository jpa;

    public RecetaJpaAdapter(RecetaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<RecetaMedica> buscarPorId(String id) {
        return jpa.findById(id).map(this::aDominio);
    }

    @Override
    public RecetaMedica guardar(RecetaMedica r) {
        RecetaJpaEntity e = jpa.save(new RecetaJpaEntity(
                r.recetaId(), r.episodioId(), r.pacienteId(), r.especialistaId(), r.medicamentos(),
                r.firma().hashSha256(), r.firma().kmsKeyId(), r.firma().firmadaEn(),
                r.enviadaFarmacia(), r.version()));
        return aDominio(e);
    }

    private RecetaMedica aDominio(RecetaJpaEntity e) {
        FirmaKms firma = new FirmaKms(e.getFirmaKmsHash(), e.getKmsKeyId(), e.getFirmadaEn());
        return new RecetaMedica(e.getRecetaId(), e.getEpisodioId(), e.getPacienteId(),
                e.getEspecialistaId(), e.getMedicamentos(), firma, e.isEnviadaFarmacia(), e.getVersion());
    }
}
