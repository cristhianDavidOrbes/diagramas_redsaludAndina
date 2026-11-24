package co.edu.ucc.redsalud.auditoria.infraestructura.salida.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "trazas_acceso", schema = "auditoria")
public class TrazaAuditoriaJpaEntity {

    @Id
    @Column(name = "traza_id")
    private String trazaId;
    @Column(name = "usuario_id", nullable = false)
    private String usuarioId;
    @Column(nullable = false)
    private String rol;
    @Column(name = "recurso_id", nullable = false)
    private String recursoId;
    @Column(nullable = false)
    private String motivo;
    @Column(nullable = false)
    private boolean permitido;
    @Column(name = "centro_id", nullable = false)
    private String centroId;
    @Column(name = "ocurrido_en", nullable = false)
    private Instant ocurridoEn;
    @Column(name = "hash_anterior", nullable = false)
    private String hashAnterior;
    @Column(name = "hash_actual", nullable = false)
    private String hashActual;

    protected TrazaAuditoriaJpaEntity() {}

    public TrazaAuditoriaJpaEntity(String trazaId, String usuarioId, String rol, String recursoId, String motivo,
                                   boolean permitido, String centroId, Instant ocurridoEn, String hashAnterior, String hashActual) {
        this.trazaId = trazaId; this.usuarioId = usuarioId; this.rol = rol; this.recursoId = recursoId;
        this.motivo = motivo; this.permitido = permitido; this.centroId = centroId;
        this.ocurridoEn = ocurridoEn; this.hashAnterior = hashAnterior; this.hashActual = hashActual;
    }

    public String getTrazaId() { return trazaId; }
    public String getUsuarioId() { return usuarioId; }
    public String getRol() { return rol; }
    public String getRecursoId() { return recursoId; }
    public String getMotivo() { return motivo; }
    public boolean isPermitido() { return permitido; }
    public String getCentroId() { return centroId; }
    public Instant getOcurridoEn() { return ocurridoEn; }
    public String getHashAnterior() { return hashAnterior; }
    public String getHashActual() { return hashActual; }
}
