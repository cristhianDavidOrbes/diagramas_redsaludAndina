package co.edu.ucc.redsalud.agenda.infraestructura.salida.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

@Entity
@Table(name = "cupos_especialista", schema = "agenda")
public class CupoJpaEntity {

    @Id
    @Column(name = "cupo_id")
    private String cupoId;

    @Column(name = "especialista_id", nullable = false)
    private String especialistaId;

    @Column(name = "centro_id", nullable = false)
    private String centroId;

    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora;

    @Column(nullable = false)
    private boolean reservado;

    @Column(name = "paciente_id")
    private String pacienteId;

    @Column(name = "interconsulta_id")
    private String interconsultaId;

    @Version
    private long version;

    protected CupoJpaEntity() {}

    public CupoJpaEntity(String cupoId, String especialistaId, String centroId, Instant fechaHora,
                         boolean reservado, String pacienteId, String interconsultaId, long version) {
        this.cupoId = cupoId;
        this.especialistaId = especialistaId;
        this.centroId = centroId;
        this.fechaHora = fechaHora;
        this.reservado = reservado;
        this.pacienteId = pacienteId;
        this.interconsultaId = interconsultaId;
        this.version = version;
    }

    public String getCupoId() { return cupoId; }
    public String getEspecialistaId() { return especialistaId; }
    public String getCentroId() { return centroId; }
    public Instant getFechaHora() { return fechaHora; }
    public boolean isReservado() { return reservado; }
    public String getPacienteId() { return pacienteId; }
    public String getInterconsultaId() { return interconsultaId; }
    public long getVersion() { return version; }
}
