package co.edu.ucc.redsalud.interconsulta.infraestructura.entrada;

import co.edu.ucc.redsalud.interconsulta.aplicacion.puertos.entrada.GestionarInterconsultaUseCase;
import co.edu.ucc.redsalud.interconsulta.dominio.EstadoDerivacion;
import co.edu.ucc.redsalud.interconsulta.dominio.Interconsulta;
import co.edu.ucc.redsalud.interconsulta.dominio.InterconsultaException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/interconsultas")
public class InterconsultaController {

    private final GestionarInterconsultaUseCase useCase;

    public InterconsultaController(GestionarInterconsultaUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<Interconsulta> solicitar(@RequestBody SolicitudRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(useCase.solicitar(req.interconsultaId(), req.pacienteId(), req.centroOrigenId(), req.especialidad()));
    }

    @PatchMapping("/{id}/estado/{nuevoEstado}")
    public ResponseEntity<Interconsulta> avanzar(@PathVariable String id, @PathVariable EstadoDerivacion nuevoEstado) {
        return ResponseEntity.ok(useCase.avanzarEstado(id, nuevoEstado));
    }

    @ExceptionHandler(InterconsultaException.class)
    public ResponseEntity<Map<String, String>> error(InterconsultaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    public record SolicitudRequest(String interconsultaId, String pacienteId, String centroOrigenId, String especialidad) {}
}
