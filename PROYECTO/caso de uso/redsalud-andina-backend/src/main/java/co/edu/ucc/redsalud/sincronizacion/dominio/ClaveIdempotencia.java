package co.edu.ucc.redsalud.sincronizacion.dominio;

public record ClaveIdempotencia(String valor) {

    public ClaveIdempotencia {
        if (valor == null || valor.isBlank()) {
            throw new SincronizacionException("Idempotency-Key (UUID/ULID) obligatoria");
        }
    }
}
