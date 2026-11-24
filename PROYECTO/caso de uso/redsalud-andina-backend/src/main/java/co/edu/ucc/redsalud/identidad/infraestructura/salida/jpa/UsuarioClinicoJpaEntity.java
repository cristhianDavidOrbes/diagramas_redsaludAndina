package co.edu.ucc.redsalud.identidad.infraestructura.salida.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "usuarios_clinicos", schema = "identidad")
public class UsuarioClinicoJpaEntity {

    @Id
    @Column(name = "usuario_id")
    private String usuarioId;

    @Column(nullable = false)
    private String rol;

    @Column(name = "centro_id", nullable = false)
    private String centroId;

    private String especialidad;

    @Column(nullable = false)
    private boolean activo;

    @Version
    private long version;

    protected UsuarioClinicoJpaEntity() {}

    public UsuarioClinicoJpaEntity(String usuarioId, String rol, String centroId,
                                   String especialidad, boolean activo, long version) {
        this.usuarioId = usuarioId;
        this.rol = rol;
        this.centroId = centroId;
        this.especialidad = especialidad;
        this.activo = activo;
        this.version = version;
    }

    public String getUsuarioId() { return usuarioId; }
    public String getRol() { return rol; }
    public String getCentroId() { return centroId; }
    public String getEspecialidad() { return especialidad; }
    public boolean isActivo() { return activo; }
    public long getVersion() { return version; }
}
