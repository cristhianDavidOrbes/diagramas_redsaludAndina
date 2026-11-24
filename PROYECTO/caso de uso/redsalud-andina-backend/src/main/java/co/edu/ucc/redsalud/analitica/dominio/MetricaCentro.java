package co.edu.ucc.redsalud.analitica.dominio;

import java.util.Objects;

public record MetricaCentro(
        String centroId,
        long teleconsultas,
        double disponibilidadPct,
        double p95SyncMinutos) {

    public MetricaCentro {
        Objects.requireNonNull(centroId, "centroId obligatorio");
        if (disponibilidadPct < 0 || disponibilidadPct > 100) {
            throw new AnaliticaException("Disponibilidad fuera de rango: " + disponibilidadPct);
        }
    }

    public boolean cumpleSlo() {
        return disponibilidadPct >= 99.5 && p95SyncMinutos <= 15.0;
    }
}
