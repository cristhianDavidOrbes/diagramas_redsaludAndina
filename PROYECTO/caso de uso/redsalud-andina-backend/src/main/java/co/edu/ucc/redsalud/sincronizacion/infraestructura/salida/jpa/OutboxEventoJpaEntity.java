package co.edu.ucc.redsalud.sincronizacion.infraestructura.salida.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "outbox_eventos", schema = "sincronizacion")
public class OutboxEventoJpaEntity {

    @Id
    @Column(name = "evento_id")
    private String eventoId;

    @Column(name = "agregado_id", nullable = false)
    private String agregadoId;

    @Column(name = "tipo_evento", nullable = false)
    private String tipoEvento;

    @Column(nullable = false)
    private String payload;

    @Column(nullable = false)
    private boolean publicado;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    protected OutboxEventoJpaEntity() {}

    public OutboxEventoJpaEntity(String eventoId, String agregadoId, String tipoEvento,
                                 String payload, boolean publicado, Instant creadoEn) {
        this.eventoId = eventoId;
        this.agregadoId = agregadoId;
        this.tipoEvento = tipoEvento;
        this.payload = payload;
        this.publicado = publicado;
        this.creadoEn = creadoEn;
    }

    public void marcarPublicado() { this.publicado = true; }
    public String getEventoId() { return eventoId; }
    public String getAgregadoId() { return agregadoId; }
    public String getTipoEvento() { return tipoEvento; }
    public String getPayload() { return payload; }
    public boolean isPublicado() { return publicado; }
    public Instant getCreadoEn() { return creadoEn; }
}
