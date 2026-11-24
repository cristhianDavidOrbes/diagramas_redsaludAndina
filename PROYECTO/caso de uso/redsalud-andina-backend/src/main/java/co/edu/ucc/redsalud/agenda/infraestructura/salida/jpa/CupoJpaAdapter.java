package co.edu.ucc.redsalud.agenda.infraestructura.salida.jpa;

import co.edu.ucc.redsalud.agenda.aplicacion.puertos.salida.CupoRepository;
import co.edu.ucc.redsalud.agenda.dominio.CupoEspecialista;
import java.util.Optional;

public class CupoJpaAdapter implements CupoRepository {

    private final CupoJpaRepository jpa;

    public CupoJpaAdapter(CupoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<CupoEspecialista> buscarPorId(String cupoId) {
        return jpa.findById(cupoId).map(this::aDominio);
    }

    @Override
    public CupoEspecialista guardar(CupoEspecialista c) {
        CupoJpaEntity guardado = jpa.save(new CupoJpaEntity(
                c.cupoId(), c.especialistaId(), c.centroId(), c.fechaHora(),
                c.reservado(), c.pacienteId(), c.interconsultaId(), c.version()));
        return aDominio(guardado);
    }

    private CupoEspecialista aDominio(CupoJpaEntity e) {
        return new CupoEspecialista(e.getCupoId(), e.getEspecialistaId(), e.getCentroId(),
                e.getFechaHora(), e.isReservado(), e.getPacienteId(), e.getInterconsultaId(), e.getVersion());
    }
}
