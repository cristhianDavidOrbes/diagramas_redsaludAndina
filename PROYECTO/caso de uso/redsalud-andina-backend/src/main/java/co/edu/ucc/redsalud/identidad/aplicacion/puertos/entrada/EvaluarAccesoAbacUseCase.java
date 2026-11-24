package co.edu.ucc.redsalud.identidad.aplicacion.puertos.entrada;

import co.edu.ucc.redsalud.identidad.dominio.DecisionAcceso;

public interface EvaluarAccesoAbacUseCase {

    DecisionAcceso evaluar(String usuarioId, String pacienteId, String centroRecurso, String recursoId, String motivo);

    DecisionAcceso exigirAccesoClinico(String usuarioId, String pacienteId, String centroRecurso,
                                       String recursoId, String motivo);
}
