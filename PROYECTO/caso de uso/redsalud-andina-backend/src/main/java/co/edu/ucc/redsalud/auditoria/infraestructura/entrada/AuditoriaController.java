package co.edu.ucc.redsalud.auditoria.infraestructura.entrada;

import co.edu.ucc.redsalud.auditoria.aplicacion.puertos.entrada.AuditarAccesoUseCase;
import co.edu.ucc.redsalud.auditoria.dominio.ReporteAuditoria;
import co.edu.ucc.redsalud.auditoria.dominio.TrazaAccesoClinico;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auditoria")
public class AuditoriaController {

    private final AuditarAccesoUseCase useCase;

    public AuditoriaController(AuditarAccesoUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/trazas")
    public ResponseEntity<TrazaAccesoClinico> registrar(@RequestBody TrazaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.registrar(
                req.usuarioId(), req.rol(), req.recursoId(), req.motivo(), req.permitido(), req.centroId()));
    }

    @GetMapping("/reporte/{recursoId}")
    public ResponseEntity<ReporteAuditoria> reporte(@PathVariable String recursoId) {
        return ResponseEntity.ok(useCase.generarReporte(recursoId));
    }

    public record TrazaRequest(String usuarioId, String rol, String recursoId,
                               String motivo, boolean permitido, String centroId) {}
}
