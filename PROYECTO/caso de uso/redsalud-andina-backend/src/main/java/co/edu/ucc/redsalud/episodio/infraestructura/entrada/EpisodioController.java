package co.edu.ucc.redsalud.episodio.infraestructura.entrada;

import co.edu.ucc.redsalud.episodio.aplicacion.puertos.entrada.RegistrarEpisodioUseCase;
import co.edu.ucc.redsalud.episodio.dominio.EpisodioClinico;
import co.edu.ucc.redsalud.episodio.dominio.EpisodioException;
import java.nio.charset.StandardCharsets;
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
@RequestMapping("/api/v1/episodios")
public class EpisodioController {

    private final RegistrarEpisodioUseCase useCase;

    public EpisodioController(RegistrarEpisodioUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<EpisodioClinico> registrar(@RequestBody NuevoEpisodioRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.registrar(
                req.episodioId(), req.pacienteId(), req.centroId(), req.notaEvolucion(), req.signosVitales()));
    }

    @PostMapping("/{episodioId}/adjuntos")
    public ResponseEntity<EpisodioClinico> adjuntar(@PathVariable String episodioId, @RequestBody AdjuntoRequest req) {
        byte[] bytes = req.contenidoBase64().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok(useCase.adjuntarExamen(episodioId, req.nombreArchivo(), bytes));
    }

    @ExceptionHandler(EpisodioException.class)
    public ResponseEntity<Map<String, String>> error(EpisodioException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    public record NuevoEpisodioRequest(String episodioId, String pacienteId, String centroId,
                                       String notaEvolucion, String signosVitales) {}
    public record AdjuntoRequest(String nombreArchivo, String contenidoBase64) {}
}
