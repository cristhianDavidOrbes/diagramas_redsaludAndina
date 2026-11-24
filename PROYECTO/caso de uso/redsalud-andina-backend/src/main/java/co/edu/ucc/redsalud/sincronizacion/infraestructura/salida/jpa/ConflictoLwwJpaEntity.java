package co.edu.ucc.redsalud.sincronizacion.infraestructura.salida.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "conflictos_lww", schema = "sincronizacion")
public class ConflictoLwwJpaEntity {

    @Id
    @Column(name = "conflicto_id")
    private String conflictoId;

    @Column(name = "agregado_id", nullable = false)
    private String agregadoId;

    @Column(nullable = false)
    private String campo;

    @Column(name = "valor_ganador", nullable = false)
    private String valorGanador;

    @Column(name = "valor_desplazado", nullable = false)
    private String valorDesplazado;

    @Column(name = "registrado_en", nullable = false)
    private Instant registradoEn;

    protected ConflictoLwwJpaEntity() {}

    public ConflictoLwwJpaEntity(String conflictoId, String agregadoId, String campo,
                                 String valorGanador, String valorDesplazado, Instant registradoEn) {
        this.conflictoId = conflictoId;
        this.agregadoId = agregadoId;
        this.campo = campo;
        this.valorGanador = valorGanador;
        this.valorDesplazado = valorDesplazado;
        this.registradoEn = registradoEn;
    }

    public String getConflictoId() { return conflictoId; }
    public String getAgregadoId() { return agregadoId; }
    public String getCampo() { return campo; }
    public String getValorGanador() { return valorGanador; }
    public String getValorDesplazado() { return valorDesplazado; }
    public Instant getRegistradoEn() { return registradoEn; }
}
