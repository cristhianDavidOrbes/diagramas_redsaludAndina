package co.edu.ucc.redsalud.receta.infraestructura.salida.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

@Entity
@Table(name = "recetas_medicas", schema = "receta")
public class RecetaJpaEntity {

    @Id
    @Column(name = "receta_id")
    private String recetaId;

    @Column(name = "episodio_id", nullable = false)
    private String episodioId;

    @Column(name = "paciente_id", nullable = false)
    private String pacienteId;

    @Column(name = "especialista_id", nullable = false)
    private String especialistaId;

    @Column(nullable = false)
    private String medicamentos;

    @Column(name = "firma_kms_hash", nullable = false)
    private String firmaKmsHash;

    @Column(name = "kms_key_id", nullable = false)
    private String kmsKeyId;

    @Column(name = "firmada_en", nullable = false)
    private Instant firmadaEn;

    @Column(name = "enviada_farmacia", nullable = false)
    private boolean enviadaFarmacia;

    @Version
    private long version;

    protected RecetaJpaEntity() {}

    public RecetaJpaEntity(String recetaId, String episodioId, String pacienteId, String especialistaId,
                           String medicamentos, String firmaKmsHash, String kmsKeyId,
                           Instant firmadaEn, boolean enviadaFarmacia, long version) {
        this.recetaId = recetaId;
        this.episodioId = episodioId;
        this.pacienteId = pacienteId;
        this.especialistaId = especialistaId;
        this.medicamentos = medicamentos;
        this.firmaKmsHash = firmaKmsHash;
        this.kmsKeyId = kmsKeyId;
        this.firmadaEn = firmadaEn;
        this.enviadaFarmacia = enviadaFarmacia;
        this.version = version;
    }

    public String getRecetaId() { return recetaId; }
    public String getEpisodioId() { return episodioId; }
    public String getPacienteId() { return pacienteId; }
    public String getEspecialistaId() { return especialistaId; }
    public String getMedicamentos() { return medicamentos; }
    public String getFirmaKmsHash() { return firmaKmsHash; }
    public String getKmsKeyId() { return kmsKeyId; }
    public Instant getFirmadaEn() { return firmadaEn; }
    public boolean isEnviadaFarmacia() { return enviadaFarmacia; }
    public long getVersion() { return version; }
}
