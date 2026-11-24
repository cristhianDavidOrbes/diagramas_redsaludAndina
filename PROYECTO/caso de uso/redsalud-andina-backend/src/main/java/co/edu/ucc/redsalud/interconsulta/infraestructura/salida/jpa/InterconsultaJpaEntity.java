package co.edu.ucc.redsalud.interconsulta.infraestructura.salida.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

@Entity
@Table(name = "interconsultas", schema = "interconsulta")
public class InterconsultaJpaEntity {

    @Id
    @Column(name = "interconsulta_id")
    private String interconsultaId;

    @Column(name = "paciente_id", nullable = false)
    private String pacienteId;

    @Column(name = "centro_origen_id", nullable = false)
    private String centroOrigenId;

    @Column(nullable = false)
    private String especialidad;

    @Column(nullable = false)
    private String estado;

    @Column(name = "creada_en", nullable = false)
    private Instant creadaEn;

    @Version
    private long version;

    protected InterconsultaJpaEntity() {}

    public InterconsultaJpaEntity(String interconsultaId, String pacienteId, String centroOrigenId,
                                  String especialidad, String estado, Instant creadaEn, long version) {
        this.interconsultaId = interconsultaId;
        this.pacienteId = pacienteId;
        this.centroOrigenId = centroOrigenId;
        this.especialidad = especialidad;
        this.estado = estado;
        this.creadaEn = creadaEn;
        this.version = version;
    }

    public String getInterconsultaId() { return interconsultaId; }
    public String getPacienteId() { return pacienteId; }
    public String getCentroOrigenId() { return centroOrigenId; }
    public String getEspecialidad() { return especialidad; }
    public String getEstado() { return estado; }
    public Instant getCreadaEn() { return creadaEn; }
    public long getVersion() { return version; }
}
