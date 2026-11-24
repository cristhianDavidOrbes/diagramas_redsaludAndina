package co.edu.ucc.redsalud.agenda.aplicacion.servicios;

import co.edu.ucc.redsalud.agenda.aplicacion.puertos.entrada.ReservarCupoUseCase;
import co.edu.ucc.redsalud.agenda.aplicacion.puertos.salida.CupoRepository;
import co.edu.ucc.redsalud.agenda.dominio.AgendaException;
import co.edu.ucc.redsalud.agenda.dominio.CitaTelemedicina;
import co.edu.ucc.redsalud.agenda.dominio.CupoEspecialista;

public class ReservarCupoService implements ReservarCupoUseCase {

    private final CupoRepository repositorio;

    public ReservarCupoService(CupoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public CupoEspecialista publicarCupo(CupoEspecialista cupo) {
        return repositorio.guardar(cupo);
    }

    @Override
    public CitaTelemedicina reservar(String cupoId, String pacienteId, String interconsultaId) {
        CupoEspecialista cupo = repositorio.buscarPorId(cupoId)
                .orElseThrow(() -> new AgendaException("Cupo no encontrado: " + cupoId));
        CitaTelemedicina cita = cupo.reservar(pacienteId, interconsultaId);
        repositorio.guardar(cupo);
        return cita;
    }
}
