package co.edu.ucc.redsalud.receta.infraestructura.entrada;

import co.edu.ucc.redsalud.receta.aplicacion.puertos.entrada.EmitirRecetaUseCase;
import co.edu.ucc.redsalud.receta.dominio.RecetaException;
import co.edu.ucc.redsalud.receta.dominio.RecetaMedica;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recetas")
public class RecetaController {

    private final EmitirRecetaUseCase useCase;

    public RecetaController(EmitirRecetaUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<RecetaMedica> emitir(@RequestBody EmitirRecetaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.emitir(
                req.recetaId(), req.episodioId(), req.pacienteId(), req.especialistaId(), req.medicamentos()));
    }

    @ExceptionHandler(RecetaException.class)
    public ResponseEntity<Map<String, String>> error(RecetaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    public record EmitirRecetaRequest(String recetaId, String episodioId, String pacienteId,
                                      String especialistaId, String medicamentos) {}
}
