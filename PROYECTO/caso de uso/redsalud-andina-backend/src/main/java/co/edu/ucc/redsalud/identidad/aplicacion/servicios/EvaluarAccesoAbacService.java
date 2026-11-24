package co.edu.ucc.redsalud.identidad.aplicacion.servicios;

import co.edu.ucc.redsalud.identidad.aplicacion.puertos.entrada.EvaluarAccesoAbacUseCase;
import co.edu.ucc.redsalud.identidad.aplicacion.puertos.salida.IdentidadRepository;
import co.edu.ucc.redsalud.identidad.aplicacion.puertos.salida.RegistroDenegacionPort;
import co.edu.ucc.redsalud.identidad.dominio.AutorizadorAbac;
import co.edu.ucc.redsalud.identidad.dominio.DecisionAcceso;
import co.edu.ucc.redsalud.identidad.dominio.IdentidadException;
import co.edu.ucc.redsalud.identidad.dominio.PrincipalClinico;

public class EvaluarAccesoAbacService implements EvaluarAccesoAbacUseCase {

    private final IdentidadRepository repositorio;
    private final RegistroDenegacionPort registro;
    private final AutorizadorAbac autorizador = new AutorizadorAbac();

    public EvaluarAccesoAbacService(IdentidadRepository repositorio, RegistroDenegacionPort registro) {
        this.repositorio = repositorio;
        this.registro = registro;
    }

    @Override
    public DecisionAcceso evaluar(String usuarioId, String pacienteId, String centroRecurso,
                                  String recursoId, String motivo) {
        PrincipalClinico principal = repositorio.buscarPorId(usuarioId).orElse(null);
        DecisionAcceso decision = autorizador.evaluar(principal, pacienteId, centroRecurso, recursoId, motivo);
        registro.registrarDecision(decision);
        return decision;
    }

    @Override
    public DecisionAcceso exigirAccesoClinico(String usuarioId, String pacienteId, String centroRecurso,
                                              String recursoId, String motivo) {
        DecisionAcceso decision = evaluar(usuarioId, pacienteId, centroRecurso, recursoId, motivo);
        if (!decision.permitido()) {
            throw new IdentidadException("Acceso denegado: " + decision.razon());
        }
        return decision;
    }
}
