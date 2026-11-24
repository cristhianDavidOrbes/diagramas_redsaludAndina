package co.edu.ucc.redsalud.legado.aplicacion.puertos.entrada;

import co.edu.ucc.redsalud.legado.dominio.HistorialCanonico;

public interface ConsultarHistorialUseCase {

    HistorialCanonico consultar(String pacienteId, String motivo);
}
