package co.edu.ucc.redsalud.interconsulta.infraestructura.salida.jpa;

import co.edu.ucc.redsalud.interconsulta.aplicacion.puertos.salida.InterconsultaRepository;
import co.edu.ucc.redsalud.interconsulta.dominio.EstadoDerivacion;
import co.edu.ucc.redsalud.interconsulta.dominio.Interconsulta;
import java.util.Optional;

public class InterconsultaJpaAdapter implements InterconsultaRepository {

    private final InterconsultaJpaRepository jpa;

    public InterconsultaJpaAdapter(InterconsultaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Interconsulta> buscarPorId(String id) {
        return jpa.findById(id).map(this::aDominio);
    }

    @Override
    public Interconsulta guardar(Interconsulta i) {
        InterconsultaJpaEntity e = jpa.save(new InterconsultaJpaEntity(
                i.interconsultaId(), i.pacienteId(), i.centroOrigenId(),
                i.especialidad(), i.estado().name(), i.creadaEn(), i.version()));
        return aDominio(e);
    }

    private Interconsulta aDominio(InterconsultaJpaEntity e) {
        return new Interconsulta(e.getInterconsultaId(), e.getPacienteId(), e.getCentroOrigenId(),
                e.getEspecialidad(), EstadoDerivacion.valueOf(e.getEstado()), e.getCreadaEn(), e.getVersion());
    }
}
