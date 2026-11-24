package co.edu.ucc.redsalud.episodio.dominio;

import java.time.Instant;
import java.util.Objects;

public class EpisodioClinico {

    private final String episodioId;
    private final String pacienteId;
    private final String centroId;
    private String notaEvolucion;
    private String signosVitales;
    private AdjuntoCifrado adjunto;
    private Instant actualizadoEn;
    private final long version;

    public EpisodioClinico(String episodioId, String pacienteId, String centroId,
                           String notaEvolucion, String signosVitales,
                           AdjuntoCifrado adjunto, Instant actualizadoEn, long version) {
        this.episodioId = Objects.requireNonNull(episodioId, "episodioId obligatorio");
        this.pacienteId = Objects.requireNonNull(pacienteId, "pacienteId obligatorio");
        this.centroId = Objects.requireNonNull(centroId, "centroId obligatorio");
        this.notaEvolucion = Objects.requireNonNull(notaEvolucion, "notaEvolucion obligatoria");
        this.signosVitales = Objects.requireNonNull(signosVitales, "signosVitales obligatorios");
        this.adjunto = adjunto;
        this.actualizadoEn = Objects.requireNonNull(actualizadoEn, "actualizadoEn obligatorio");
        this.version = version;
    }

    public void adjuntar(AdjuntoCifrado nuevoAdjunto, String regionPermitida) {
        nuevoAdjunto.validarResidencia(regionPermitida);
        this.adjunto = nuevoAdjunto;
        this.actualizadoEn = Instant.now();
    }

    public String episodioId() { return episodioId; }
    public String pacienteId() { return pacienteId; }
    public String centroId() { return centroId; }
    public String notaEvolucion() { return notaEvolucion; }
    public String signosVitales() { return signosVitales; }
    public AdjuntoCifrado adjunto() { return adjunto; }
    public Instant actualizadoEn() { return actualizadoEn; }
    public long version() { return version; }
}
