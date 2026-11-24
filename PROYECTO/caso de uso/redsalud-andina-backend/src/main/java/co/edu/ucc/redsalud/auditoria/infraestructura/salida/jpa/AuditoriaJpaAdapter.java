package co.edu.ucc.redsalud.auditoria.infraestructura.salida.jpa;

import co.edu.ucc.redsalud.auditoria.aplicacion.puertos.salida.TrazaAuditoriaRepository;
import co.edu.ucc.redsalud.auditoria.dominio.TrazaAccesoClinico;
import java.util.List;
import java.util.Optional;

public class AuditoriaJpaAdapter implements TrazaAuditoriaRepository {

    private final TrazaAuditoriaJpaRepository jpa;

    public AuditoriaJpaAdapter(TrazaAuditoriaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public TrazaAccesoClinico guardarAppendOnly(TrazaAccesoClinico t) {
        jpa.save(new TrazaAuditoriaJpaEntity(t.trazaId(), t.usuarioId(), t.rol(), t.recursoId(),
                t.motivo(), t.permitido(), t.centroId(), t.ocurridoEn(), t.hashAnterior(), t.hashActual()));
        return t;
    }

    @Override
    public Optional<String> ultimoHash() {
        List<TrazaAuditoriaJpaEntity> todas = jpa.findAllByOrderByOcurridoEnAsc();
        return todas.isEmpty() ? Optional.empty() : Optional.of(todas.getLast().getHashActual());
    }

    @Override
    public List<TrazaAccesoClinico> buscarPorRecurso(String recursoId) {
        return jpa.findByRecursoIdOrderByOcurridoEnAsc(recursoId).stream().map(this::aDominio).toList();
    }

    @Override
    public List<TrazaAccesoClinico> listarTodas() {
        return jpa.findAllByOrderByOcurridoEnAsc().stream().map(this::aDominio).toList();
    }

    private TrazaAccesoClinico aDominio(TrazaAuditoriaJpaEntity e) {
        return new TrazaAccesoClinico(e.getTrazaId(), e.getUsuarioId(), e.getRol(), e.getRecursoId(),
                e.getMotivo(), e.isPermitido(), e.getCentroId(), e.getOcurridoEn(), e.getHashAnterior(), e.getHashActual());
    }
}
