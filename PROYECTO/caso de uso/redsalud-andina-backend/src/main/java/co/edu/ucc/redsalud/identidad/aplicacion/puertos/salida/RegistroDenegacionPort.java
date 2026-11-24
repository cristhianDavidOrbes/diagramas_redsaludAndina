package co.edu.ucc.redsalud.identidad.aplicacion.puertos.salida;

import co.edu.ucc.redsalud.identidad.dominio.DecisionAcceso;
import java.util.List;

public interface RegistroDenegacionPort {

    void registrarDecision(DecisionAcceso decision);

    List<DecisionAcceso> listarDenegaciones();
}
