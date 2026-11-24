package co.edu.ucc.redsalud.receta.dominio;

import java.time.Instant;
import java.util.Objects;

public record FirmaKms(String hashSha256, String kmsKeyId, Instant firmadaEn) {

    public FirmaKms {
        if (hashSha256 == null || hashSha256.isBlank()) {
            throw new RecetaException("Firma KMS obligatoria para emitir receta");
        }
        Objects.requireNonNull(kmsKeyId, "kmsKeyId obligatorio");
        Objects.requireNonNull(firmadaEn, "firmadaEn obligatorio");
    }
}
