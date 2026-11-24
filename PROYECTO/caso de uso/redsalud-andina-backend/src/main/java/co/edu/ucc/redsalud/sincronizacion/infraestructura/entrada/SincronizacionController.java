package co.edu.ucc.redsalud.sincronizacion.infraestructura.entrada;

import co.edu.ucc.redsalud.sincronizacion.aplicacion.puertos.entrada.SincronizarEpisodiosUseCase;
import co.edu.ucc.redsalud.sincronizacion.dominio.ClaveIdempotencia;
import co.edu.ucc.redsalud.sincronizacion.dominio.LoteSincronizacion;
import co.edu.ucc.redsalud.sincronizacion.dominio.ResultadoSincronizacion;
import co.edu.ucc.redsalud.sincronizacion.dominio.TipoOperacionOffline;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SincronizacionController {

    private final SincronizarEpisodiosUseCase useCase;

    public SincronizacionController(SincronizarEpisodiosUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/sync/episodios")
    public ResponseEntity<ResultadoSincronizacion> sincronizar(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody SyncRequest req) {
        LoteSincronizacion lote = new LoteSincronizacion(
                new ClaveIdempotencia(idempotencyKey), req.agregadoId(), req.tipo(), req.campo(),
                req.valorEntrante(), req.selloTiempoCentro(), req.valorPrevio(), req.selloTiempoPrevio());
        ResultadoSincronizacion res = useCase.sincronizar(lote);
        return ResponseEntity.status(res.codigoHttp()).body(res);
    }

    public record SyncRequest(String agregadoId, TipoOperacionOffline tipo, String campo,
                              String valorEntrante, Instant selloTiempoCentro,
                              String valorPrevio, Instant selloTiempoPrevio) {}
}
