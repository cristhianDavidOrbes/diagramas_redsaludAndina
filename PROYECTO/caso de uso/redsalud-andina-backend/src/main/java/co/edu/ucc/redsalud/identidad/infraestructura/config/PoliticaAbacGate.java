package co.edu.ucc.redsalud.identidad.infraestructura.config;

import co.edu.ucc.redsalud.identidad.aplicacion.puertos.entrada.EvaluarAccesoAbacUseCase;
import co.edu.ucc.redsalud.identidad.dominio.DecisionAcceso;
import org.springframework.security.access.prepost.PreAuthorize;

public class PoliticaAbacGate {

    private final EvaluarAccesoAbacUseCase useCase;

    public PoliticaAbacGate(EvaluarAccesoAbacUseCase useCase) {
        this.useCase = useCase;
    }

    @PreAuthorize("@abacGuard.permitir(#usuarioId, #pacienteId, #centroRecurso, #recursoId, #motivo)")
    public DecisionAcceso autorizarEnCapaServicio(String usuarioId, String pacienteId,
                                                  String centroRecurso, String recursoId, String motivo) {
        return useCase.evaluar(usuarioId, pacienteId, centroRecurso, recursoId, motivo);
    }
}
