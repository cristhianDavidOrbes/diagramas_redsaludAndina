package co.edu.ucc.redsalud.episodio.dominio;

import java.util.Objects;

public record AdjuntoCifrado(
        String s3Key,
        String kmsKeyId,
        String regionAws,
        boolean replicacionExterna) {

    public AdjuntoCifrado {
        Objects.requireNonNull(s3Key, "s3Key obligatorio");
        if (kmsKeyId == null || kmsKeyId.isBlank()) {
            throw new EpisodioException("Todo adjunto clínico exige cifrado KMS");
        }
        if (replicacionExterna) {
            throw new EpisodioException("Prohibida replicación cross-region en adjuntos clínicos (ASR-6)");
        }
    }

    public void validarResidencia(String regionPermitida) {
        if (!Objects.equals(regionPermitida, regionAws)) {
            throw new EpisodioException("Residencia violada: región " + regionAws + " != " + regionPermitida);
        }
    }
}
