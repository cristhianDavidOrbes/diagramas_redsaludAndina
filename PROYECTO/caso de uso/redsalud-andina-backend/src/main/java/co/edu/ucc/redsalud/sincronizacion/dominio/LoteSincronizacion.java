package co.edu.ucc.redsalud.sincronizacion.dominio;

import java.time.Instant;
import java.util.Objects;

public record LoteSincronizacion(
        ClaveIdempotencia clave,
        String agregadoId,
        TipoOperacionOffline tipo,
        String campo,
        String valorEntrante,
        Instant selloTiempoCentro,
        String valorPrevio,
        Instant selloTiempoPrevio) {

    public LoteSincronizacion {
        Objects.requireNonNull(clave, "clave obligatoria");
        Objects.requireNonNull(agregadoId, "agregadoId obligatorio");
        Objects.requireNonNull(tipo, "tipo obligatorio");
        Objects.requireNonNull(campo, "campo obligatorio");
        Objects.requireNonNull(valorEntrante, "valorEntrante obligatorio");
        Objects.requireNonNull(selloTiempoCentro, "selloTiempoCentro obligatorio");
    }
}
