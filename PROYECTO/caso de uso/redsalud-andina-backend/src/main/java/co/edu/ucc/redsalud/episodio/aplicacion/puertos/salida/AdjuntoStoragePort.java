package co.edu.ucc.redsalud.episodio.aplicacion.puertos.salida;

import co.edu.ucc.redsalud.episodio.dominio.AdjuntoCifrado;

public interface AdjuntoStoragePort {

    AdjuntoCifrado almacenarCifrado(String episodioId, String nombreArchivo, byte[] contenido);
}
