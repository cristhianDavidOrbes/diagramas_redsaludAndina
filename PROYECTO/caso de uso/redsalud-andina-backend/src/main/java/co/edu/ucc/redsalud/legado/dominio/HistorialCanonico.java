package co.edu.ucc.redsalud.legado.dominio;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public record HistorialCanonico(
        String pacienteId,
        String resumenAntecedentes,
        Instant fechaSincronizacion,
        MarcaFrescura frescura,
        boolean degradadoDesdeCache) {

    public HistorialCanonico {
        Objects.requireNonNull(pacienteId, "pacienteId obligatorio");
        Objects.requireNonNull(resumenAntecedentes, "resumenAntecedentes obligatorio");
        Objects.requireNonNull(fechaSincronizacion, "fechaSincronizacion obligatoria");
        Objects.requireNonNull(frescura, "frescura obligatoria");
    }

    public boolean estaVigente(Instant ahora, long ttlSegundos) {
        return Duration.between(fechaSincronizacion, ahora).getSeconds() <= ttlSegundos;
    }

    public HistorialCanonico conFrescura(MarcaFrescura nueva, boolean degradado) {
        return new HistorialCanonico(pacienteId, resumenAntecedentes, fechaSincronizacion, nueva, degradado);
    }

    public String cabeceraFrescura() {
        return frescura.valorCabecera() + "; synced-at=" + fechaSincronizacion;
    }
}
