package co.edu.ucc.redsalud.agenda.dominio;

import java.time.Instant;
import java.util.Objects;

public class CupoEspecialista {

    private final String cupoId;
    private final String especialistaId;
    private final String centroId;
    private final Instant fechaHora;
    private boolean reservado;
    private String pacienteId;
    private String interconsultaId;
    private final long version;

    public CupoEspecialista(String cupoId, String especialistaId, String centroId,
                            Instant fechaHora, boolean reservado, String pacienteId,
                            String interconsultaId, long version) {
        this.cupoId = Objects.requireNonNull(cupoId, "cupoId obligatorio");
        this.especialistaId = Objects.requireNonNull(especialistaId, "especialistaId obligatorio");
        this.centroId = Objects.requireNonNull(centroId, "centroId obligatorio");
        this.fechaHora = Objects.requireNonNull(fechaHora, "fechaHora obligatoria");
        this.reservado = reservado;
        this.pacienteId = pacienteId;
        this.interconsultaId = interconsultaId;
        this.version = version;
    }

    public CitaTelemedicina reservar(String pacienteId, String interconsultaId) {
        if (this.reservado) {
            throw new AgendaException("Cupo ya reservado (regla sin doble reserva): " + cupoId);
        }
        this.reservado = true;
        this.pacienteId = Objects.requireNonNull(pacienteId, "pacienteId obligatorio");
        this.interconsultaId = Objects.requireNonNull(interconsultaId, "interconsultaId obligatorio");
        return new CitaTelemedicina(cupoId, pacienteId, interconsultaId, fechaHora);
    }

    public String cupoId() { return cupoId; }
    public String especialistaId() { return especialistaId; }
    public String centroId() { return centroId; }
    public Instant fechaHora() { return fechaHora; }
    public boolean reservado() { return reservado; }
    public String pacienteId() { return pacienteId; }
    public String interconsultaId() { return interconsultaId; }
    public long version() { return version; }
}
