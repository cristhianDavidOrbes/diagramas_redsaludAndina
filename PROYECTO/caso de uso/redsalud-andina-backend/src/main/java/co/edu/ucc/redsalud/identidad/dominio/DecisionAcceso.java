package co.edu.ucc.redsalud.identidad.dominio;

import java.time.Instant;

public record DecisionAcceso(
        boolean permitido,
        String usuarioId,
        String recursoId,
        String motivo,
        String razon,
        Instant evaluadoEn) {
}
