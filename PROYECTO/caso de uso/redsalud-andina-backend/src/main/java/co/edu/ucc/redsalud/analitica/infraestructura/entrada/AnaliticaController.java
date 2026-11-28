package co.edu.ucc.redsalud.analitica.infraestructura.entrada;

import co.edu.ucc.redsalud.analitica.aplicacion.puertos.entrada.ConsultarIndicadoresUseCase;
import co.edu.ucc.redsalud.analitica.dominio.MetricaCentro;
import co.edu.ucc.redsalud.analitica.dominio.TableroAdopcion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnaliticaController {

    private final ConsultarIndicadoresUseCase useCase;

    public AnaliticaController(ConsultarIndicadoresUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping({"/tablero", "/api/v1/analitica/metricas"})
    public ResponseEntity<MetricaCentro> registrar(@RequestBody MetricaCentro metrica) {
        return ResponseEntity.ok(useCase.registrar(metrica));
    }

    @GetMapping({"/tablero", "/api/v1/analitica/tablero"})
    public ResponseEntity<TableroAdopcion> tablero() {
        return ResponseEntity.ok(useCase.obtenerTablero());
    }
}
