package co.edu.ucc.redsalud.agenda.aplicacion.puertos.entrada;

import co.edu.ucc.redsalud.agenda.dominio.CitaTelemedicina;
import co.edu.ucc.redsalud.agenda.dominio.CupoEspecialista;

public interface ReservarCupoUseCase {

    CupoEspecialista publicarCupo(CupoEspecialista cupo);

    CitaTelemedicina reservar(String cupoId, String pacienteId, String interconsultaId);
}
