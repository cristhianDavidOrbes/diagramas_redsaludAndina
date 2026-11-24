package co.edu.ucc.redsalud.sincronizacion.dominio;

import java.time.Instant;
import java.util.Objects;

public record ConflictoLww(
        String conflictoId,
        String agregadoId,
        String campo,
        String valorGanador,
        String valorDesplazado,
        Instant registradoEn) {

    public ConflictoLww {
        Objects.requireNonNull(conflictoId, "conflictoId obligatorio");
        Objects.requireNonNull(agregadoId, "agregadoId obligatorio");
        Objects.requireNonNull(valorGanador, "valorGanador obligatorio");
        Objects.requireNonNull(valorDesplazado, "valorDesplazado obligatorio");
    }
}
