package co.edu.ucc.redsalud.sincronizacion.dominio;

import java.time.Instant;

public record MensajeOutbox(
        String eventoId,
        String agregadoId,
        String tipoEvento,
        String payload,
        boolean publicado,
        Instant creadoEn) {
}
