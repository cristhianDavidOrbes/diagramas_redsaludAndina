package co.edu.ucc.redsalud.auditoria.aplicacion.servicios;

import co.edu.ucc.redsalud.auditoria.aplicacion.puertos.entrada.AuditarAccesoUseCase;
import co.edu.ucc.redsalud.auditoria.aplicacion.puertos.salida.ArchivoWormPort;
import co.edu.ucc.redsalud.auditoria.aplicacion.puertos.salida.TrazaAuditoriaRepository;
import co.edu.ucc.redsalud.auditoria.dominio.ReporteAuditoria;
import co.edu.ucc.redsalud.auditoria.dominio.TrazaAccesoClinico;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AuditarAccesoService implements AuditarAccesoUseCase {

    private final TrazaAuditoriaRepository repositorio;
    private final ArchivoWormPort wormPort;

    public AuditarAccesoService(TrazaAuditoriaRepository repositorio, ArchivoWormPort wormPort) {
        this.repositorio = repositorio;
        this.wormPort = wormPort;
    }

    @Override
    public synchronized TrazaAccesoClinico registrar(String usuarioId, String rol, String recursoId,
                                                     String motivo, boolean permitido, String centroId) {
        String previo = repositorio.ultimoHash().orElse(TrazaAccesoClinico.HASH_GENESIS);
        TrazaAccesoClinico traza = TrazaAccesoClinico.crearEncadenada(
                UUID.randomUUID().toString(), usuarioId, rol, recursoId, motivo, permitido, centroId, Instant.now(), previo);
        TrazaAccesoClinico guardada = repositorio.guardarAppendOnly(traza);
        wormPort.archivarCompliance(guardada);
        return guardada;
    }

    @Override
    public ReporteAuditoria generarReporte(String recursoId) {
        List<TrazaAccesoClinico> trazas = repositorio.buscarPorRecurso(recursoId);
        long permitidos = trazas.stream().filter(TrazaAccesoClinico::permitido).count();
        return new ReporteAuditoria(recursoId, Instant.now(), verificarIntegridadCadena(),
                permitidos, trazas.size() - permitidos, trazas);
    }

    @Override
    public boolean verificarIntegridadCadena() {
        String previo = TrazaAccesoClinico.HASH_GENESIS;
        for (TrazaAccesoClinico t : repositorio.listarTodas()) {
            if (!t.esIntegra(previo)) {
                return false;
            }
            previo = t.hashActual();
        }
        return true;
    }
}
