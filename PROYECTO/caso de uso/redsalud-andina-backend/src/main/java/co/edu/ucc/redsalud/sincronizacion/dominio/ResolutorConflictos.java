package co.edu.ucc.redsalud.sincronizacion.dominio;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ResolutorConflictos {

    public record Evaluacion(boolean rechazado, String valorAplicado, Optional<ConflictoLww> conflicto) {}

    public Evaluacion evaluar(LoteSincronizacion lote) {
        if (!lote.tipo().permitidaEnBorde()) {
            return new Evaluacion(true, null, Optional.empty());
        }
        if (lote.valorPrevio() == null || Objects.equals(lote.valorPrevio(), lote.valorEntrante())) {
            return new Evaluacion(false, lote.valorEntrante(), Optional.empty());
        }
        boolean ganaEntrante = lote.selloTiempoPrevio() == null
                || !lote.selloTiempoCentro().isBefore(lote.selloTiempoPrevio());
        String ganador = ganaEntrante ? lote.valorEntrante() : lote.valorPrevio();
        String desplazado = ganaEntrante ? lote.valorPrevio() : lote.valorEntrante();
        ConflictoLww auditoria = new ConflictoLww(
                UUID.randomUUID().toString(), lote.agregadoId(), lote.campo(), ganador, desplazado, Instant.now());
        return new Evaluacion(false, ganador, Optional.of(auditoria));
    }
}
