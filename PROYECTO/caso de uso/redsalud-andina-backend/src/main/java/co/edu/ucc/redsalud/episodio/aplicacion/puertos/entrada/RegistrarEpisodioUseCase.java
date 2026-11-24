package co.edu.ucc.redsalud.episodio.aplicacion.puertos.entrada;

import co.edu.ucc.redsalud.episodio.dominio.EpisodioClinico;

public interface RegistrarEpisodioUseCase {

    EpisodioClinico registrar(String episodioId, String pacienteId, String centroId,
                              String notaEvolucion, String signosVitales);

    EpisodioClinico adjuntarExamen(String episodioId, String nombreArchivo, byte[] contenido);
}
