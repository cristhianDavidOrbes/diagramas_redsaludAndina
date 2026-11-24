package co.edu.ucc.redsalud.interconsulta.aplicacion.puertos.entrada;

import co.edu.ucc.redsalud.interconsulta.dominio.EstadoDerivacion;
import co.edu.ucc.redsalud.interconsulta.dominio.Interconsulta;

public interface GestionarInterconsultaUseCase {

    Interconsulta solicitar(String interconsultaId, String pacienteId, String centroOrigenId, String especialidad);

    Interconsulta avanzarEstado(String interconsultaId, EstadoDerivacion nuevoEstado);
}
