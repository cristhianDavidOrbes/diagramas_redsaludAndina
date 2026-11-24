package co.edu.ucc.redsalud.auditoria.aplicacion.puertos.salida;

import co.edu.ucc.redsalud.auditoria.dominio.TrazaAccesoClinico;

public interface ArchivoWormPort {

    void archivarCompliance(TrazaAccesoClinico traza);
}
