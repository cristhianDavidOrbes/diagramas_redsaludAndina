package co.edu.ucc.redsalud.agenda.infraestructura.entrada;

import co.edu.ucc.redsalud.agenda.aplicacion.puertos.entrada.ReservarCupoUseCase;
import co.edu.ucc.redsalud.agenda.dominio.AgendaException;
import co.edu.ucc.redsalud.agenda.dominio.CitaTelemedicina;
import co.edu.ucc.redsalud.agenda.dominio.CupoEspecialista;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agenda/cupos")
public class AgendaController {

    private final ReservarCupoUseCase useCase;

    public AgendaController(ReservarCupoUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<CupoEspecialista> publicar(@RequestBody CrearCupoRequest req) {
        CupoEspecialista cupo = new CupoEspecialista(
                req.cupoId(), req.especialistaId(), req.centroId(), req.fechaHora(), false, null, null, 0L);
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.publicarCupo(cupo));
    }

    @PostMapping("/{cupoId}/reservar")
    public ResponseEntity<CitaTelemedicina> reservar(@PathVariable String cupoId, @RequestBody ReservaRequest req) {
        return ResponseEntity.ok(useCase.reservar(cupoId, req.pacienteId(), req.interconsultaId()));
    }

    @ExceptionHandler(AgendaException.class)
    public ResponseEntity<Map<String, String>> errorAgenda(AgendaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    public record CrearCupoRequest(String cupoId, String especialistaId, String centroId, Instant fechaHora) {}
    public record ReservaRequest(String pacienteId, String interconsultaId) {}
}
