package co.edu.ucc.redsalud.receta.aplicacion.puertos.entrada;

import co.edu.ucc.redsalud.receta.dominio.RecetaMedica;

public interface EmitirRecetaUseCase {

    RecetaMedica emitir(String recetaId, String episodioId, String pacienteId,
                        String especialistaId, String medicamentos);
}
