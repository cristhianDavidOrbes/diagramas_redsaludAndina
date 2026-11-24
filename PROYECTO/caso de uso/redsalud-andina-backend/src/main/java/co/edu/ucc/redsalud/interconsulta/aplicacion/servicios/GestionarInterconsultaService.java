package co.edu.ucc.redsalud.interconsulta.aplicacion.servicios;

import co.edu.ucc.redsalud.interconsulta.aplicacion.puertos.entrada.GestionarInterconsultaUseCase;
import co.edu.ucc.redsalud.interconsulta.aplicacion.puertos.salida.InterconsultaRepository;
import co.edu.ucc.redsalud.interconsulta.dominio.EstadoDerivacion;
import co.edu.ucc.redsalud.interconsulta.dominio.Interconsulta;
import co.edu.ucc.redsalud.interconsulta.dominio.InterconsultaException;
import java.time.Instant;

public class GestionarInterconsultaService implements GestionarInterconsultaUseCase {

    private final InterconsultaRepository repositorio;

    public GestionarInterconsultaService(InterconsultaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Interconsulta solicitar(String id, String pacienteId, String centroOrigenId, String especialidad) {
        Interconsulta nueva = new Interconsulta(
                id, pacienteId, centroOrigenId, especialidad, EstadoDerivacion.SOLICITADA, Instant.now(), 0L);
        return repositorio.guardar(nueva);
    }

    @Override
    public Interconsulta avanzarEstado(String interconsultaId, EstadoDerivacion nuevoEstado) {
        Interconsulta actual = repositorio.buscarPorId(interconsultaId)
                .orElseThrow(() -> new InterconsultaException("Interconsulta no encontrada: " + interconsultaId));
        actual.avanzarA(nuevoEstado);
        return repositorio.guardar(actual);
    }
}
