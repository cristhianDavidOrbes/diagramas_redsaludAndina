package co.edu.ucc.redsalud.agenda.dominio;

import java.time.Instant;
import java.util.Objects;

public record CitaTelemedicina(
        String cupoId,
        String pacienteId,
        String interconsultaId,
        Instant fechaHora) {

    public CitaTelemedicina {
        Objects.requireNonNull(cupoId, "cupoId obligatorio");
        Objects.requireNonNull(pacienteId, "pacienteId obligatorio");
        Objects.requireNonNull(interconsultaId, "interconsultaId obligatorio");
        Objects.requireNonNull(fechaHora, "fechaHora obligatoria");
    }
}
