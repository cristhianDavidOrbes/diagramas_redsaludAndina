package co.edu.ucc.redsalud.sincronizacion.infraestructura.salida.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "claves_idempotencia", schema = "sincronizacion",
        uniqueConstraints = @UniqueConstraint(columnNames = "idempotency_key"))
public class ClaveIdempotenciaJpaEntity {

    @Id
    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Column(name = "agregado_id", nullable = false)
    private String agregadoId;

    @Column(name = "estado_resultado", nullable = false)
    private String estadoResultado;

    @Column(name = "codigo_http", nullable = false)
    private int codigoHttp;

    @Column(nullable = false)
    private String mensaje;

    @Column(name = "procesado_en", nullable = false)
    private Instant procesadoEn;

    protected ClaveIdempotenciaJpaEntity() {}

    public ClaveIdempotenciaJpaEntity(String idempotencyKey, String agregadoId, String estadoResultado,
                                      int codigoHttp, String mensaje, Instant procesadoEn) {
        this.idempotencyKey = idempotencyKey;
        this.agregadoId = agregadoId;
        this.estadoResultado = estadoResultado;
        this.codigoHttp = codigoHttp;
        this.mensaje = mensaje;
        this.procesadoEn = procesadoEn;
    }

    public String getIdempotencyKey() { return idempotencyKey; }
    public String getAgregadoId() { return agregadoId; }
    public String getEstadoResultado() { return estadoResultado; }
    public int getCodigoHttp() { return codigoHttp; }
    public String getMensaje() { return mensaje; }
}
