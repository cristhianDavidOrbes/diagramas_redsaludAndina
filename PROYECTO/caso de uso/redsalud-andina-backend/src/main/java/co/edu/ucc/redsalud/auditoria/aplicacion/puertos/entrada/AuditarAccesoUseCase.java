package co.edu.ucc.redsalud.auditoria.aplicacion.puertos.entrada;

import co.edu.ucc.redsalud.auditoria.dominio.ReporteAuditoria;
import co.edu.ucc.redsalud.auditoria.dominio.TrazaAccesoClinico;

public interface AuditarAccesoUseCase {

    TrazaAccesoClinico registrar(String usuarioId, String rol, String recursoId,
                                 String motivo, boolean permitido, String centroId);

    ReporteAuditoria generarReporte(String recursoId);

    boolean verificarIntegridadCadena();
}
