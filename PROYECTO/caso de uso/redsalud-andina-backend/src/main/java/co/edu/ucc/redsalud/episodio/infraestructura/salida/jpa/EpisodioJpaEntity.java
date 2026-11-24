package co.edu.ucc.redsalud.episodio.infraestructura.salida.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

@Entity
@Table(name = "episodios_clinicos", schema = "episodio")
public class EpisodioJpaEntity {

    @Id
    @Column(name = "episodio_id")
    private String episodioId;

    @Column(name = "paciente_id", nullable = false)
    private String pacienteId;

    @Column(name = "centro_id", nullable = false)
    private String centroId;

    @Column(name = "nota_evolucion", nullable = false)
    private String notaEvolucion;

    @Column(name = "signos_vitales", nullable = false)
    private String signosVitales;

    @Column(name = "adjunto_s3_key")
    private String adjuntoS3Key;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    @Version
    private long version;

    protected EpisodioJpaEntity() {}

    public EpisodioJpaEntity(String episodioId, String pacienteId, String centroId, String notaEvolucion,
                             String signosVitales, String adjuntoS3Key, Instant actualizadoEn, long version) {
        this.episodioId = episodioId;
        this.pacienteId = pacienteId;
        this.centroId = centroId;
        this.notaEvolucion = notaEvolucion;
        this.signosVitales = signosVitales;
        this.adjuntoS3Key = adjuntoS3Key;
        this.actualizadoEn = actualizadoEn;
        this.version = version;
    }

    public String getEpisodioId() { return episodioId; }
    public String getPacienteId() { return pacienteId; }
    public String getCentroId() { return centroId; }
    public String getNotaEvolucion() { return notaEvolucion; }
    public String getSignosVitales() { return signosVitales; }
    public String getAdjuntoS3Key() { return adjuntoS3Key; }
    public Instant getActualizadoEn() { return actualizadoEn; }
    public long getVersion() { return version; }
}
