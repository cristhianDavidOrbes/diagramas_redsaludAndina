package co.edu.ucc.redsalud.legado.infraestructura.entrada;

import co.edu.ucc.redsalud.legado.aplicacion.puertos.entrada.ConsultarHistorialUseCase;
import co.edu.ucc.redsalud.legado.dominio.HistorialCanonico;
import co.edu.ucc.redsalud.legado.dominio.LegadoException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HistorialLegadoController {

    private final ConsultarHistorialUseCase useCase;

    public HistorialLegadoController(ConsultarHistorialUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/historial/{id}")
    public ResponseEntity<HistorialCanonico> consultar(
            @PathVariable String id,
            @RequestParam(defaultValue = "teleconsulta") String motivo) {
        HistorialCanonico historial = useCase.consultar(id, motivo);
        return ResponseEntity.ok()
                .header("X-Data-Freshness", historial.cabeceraFrescura())
                .body(historial);
    }

    @ExceptionHandler(LegadoException.class)
    public ResponseEntity<Map<String, String>> noDisponible(LegadoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }
}
