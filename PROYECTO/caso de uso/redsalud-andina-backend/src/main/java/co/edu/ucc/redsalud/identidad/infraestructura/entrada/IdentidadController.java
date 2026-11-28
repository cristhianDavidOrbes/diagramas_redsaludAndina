package co.edu.ucc.redsalud.identidad.infraestructura.entrada;

import co.edu.ucc.redsalud.identidad.aplicacion.puertos.entrada.EmitirTokenDemoUseCase;
import co.edu.ucc.redsalud.identidad.aplicacion.puertos.entrada.EvaluarAccesoAbacUseCase;
import co.edu.ucc.redsalud.identidad.dominio.DecisionAcceso;
import co.edu.ucc.redsalud.identidad.dominio.IdentidadException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IdentidadController {

    private final EvaluarAccesoAbacUseCase useCase;
    private final EmitirTokenDemoUseCase emisorToken;

    public IdentidadController(EvaluarAccesoAbacUseCase useCase, EmitirTokenDemoUseCase emisorToken) {
        this.useCase = useCase;
        this.emisorToken = emisorToken;
    }

    @PostMapping({"/auth/token", "/api/v1/identidad/token"})
    public ResponseEntity<Map<String, String>> emitirToken(@RequestBody TokenRequest req) {
        String jwt = emisorToken.emitirToken(req.usuarioId());
        return ResponseEntity.ok(Map.of("token", jwt, "tipo", "Bearer", "algoritmo", "RS256"));
    }

    @PostMapping("/api/v1/identidad/evaluar")
    public ResponseEntity<DecisionAcceso> evaluar(@RequestBody EvaluacionRequest req) {
        return ResponseEntity.ok(useCase.exigirAccesoClinico(
                req.usuarioId(), req.pacienteId(), req.centroRecurso(), req.recursoId(), req.motivo()));
    }

    @GetMapping("/api/v1/identidad/protegido/{recursoId}")
    public ResponseEntity<Map<String, String>> recursoProtegido(@PathVariable String recursoId) {
        return ResponseEntity.ok(Map.of("recursoId", recursoId, "estado", "AUTORIZADO_ABAC"));
    }

    @ExceptionHandler(IdentidadException.class)
    public ResponseEntity<Map<String, String>> denegado(IdentidadException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }

    public record TokenRequest(String usuarioId) {}
    public record EvaluacionRequest(String usuarioId, String pacienteId, String centroRecurso,
                                    String recursoId, String motivo) {}
}
