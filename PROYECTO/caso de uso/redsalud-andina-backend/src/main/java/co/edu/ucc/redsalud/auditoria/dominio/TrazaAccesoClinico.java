package co.edu.ucc.redsalud.auditoria.dominio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;

public record TrazaAccesoClinico(
        String trazaId,
        String usuarioId,
        String rol,
        String recursoId,
        String motivo,
        boolean permitido,
        String centroId,
        Instant ocurridoEn,
        String hashAnterior,
        String hashActual) {

    public static final String HASH_GENESIS = "0000000000000000000000000000000000000000000000000000000000000000";

    public TrazaAccesoClinico {
        Objects.requireNonNull(trazaId, "trazaId obligatorio");
        Objects.requireNonNull(usuarioId, "usuarioId obligatorio");
        Objects.requireNonNull(recursoId, "recursoId obligatorio");
        Objects.requireNonNull(ocurridoEn, "ocurridoEn obligatorio");
    }

    public static TrazaAccesoClinico crearEncadenada(String trazaId, String usuarioId, String rol,
                                                     String recursoId, String motivo, boolean permitido,
                                                     String centroId, Instant ocurridoEn, String hashAnterior) {
        String previo = hashAnterior == null ? HASH_GENESIS : hashAnterior;
        String actual = calcularHash(trazaId, usuarioId, rol, recursoId, motivo, permitido, centroId, ocurridoEn, previo);
        return new TrazaAccesoClinico(trazaId, usuarioId, rol, recursoId, motivo, permitido, centroId, ocurridoEn, previo, actual);
    }

    public boolean esIntegra(String hashEsperadoPrevio) {
        String esperado = calcularHash(trazaId, usuarioId, rol, recursoId, motivo, permitido, centroId, ocurridoEn, hashEsperadoPrevio);
        return hashAnterior.equals(hashEsperadoPrevio) && hashActual.equals(esperado);
    }

    private static String calcularHash(String trazaId, String usuarioId, String rol, String recursoId,
                                       String motivo, boolean permitido, String centroId,
                                       Instant ocurridoEn, String hashAnterior) {
        try {
            String carga = String.join("|", trazaId, usuarioId, rol, recursoId,
                    motivo == null ? "" : motivo, String.valueOf(permitido), centroId, ocurridoEn.toString(), hashAnterior);
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(carga.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new AuditoriaException("SHA-256 no disponible para encadenar auditoría");
        }
    }
}
