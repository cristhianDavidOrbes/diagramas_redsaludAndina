package co.edu.ucc.redsalud.interconsulta.dominio;

import java.time.Instant;
import java.util.Objects;

public class Interconsulta {

    private final String interconsultaId;
    private final String pacienteId;
    private final String centroOrigenId;
    private final String especialidad;
    private EstadoDerivacion estado;
    private final Instant creadaEn;
    private final long version;

    public Interconsulta(String interconsultaId, String pacienteId, String centroOrigenId,
                         String especialidad, EstadoDerivacion estado, Instant creadaEn, long version) {
        this.interconsultaId = Objects.requireNonNull(interconsultaId, "interconsultaId obligatorio");
        this.pacienteId = Objects.requireNonNull(pacienteId, "pacienteId obligatorio");
        this.centroOrigenId = Objects.requireNonNull(centroOrigenId, "centroOrigenId obligatorio");
        this.especialidad = Objects.requireNonNull(especialidad, "especialidad obligatoria");
        this.estado = Objects.requireNonNull(estado, "estado obligatorio");
        this.creadaEn = Objects.requireNonNull(creadaEn, "creadaEn obligatorio");
        this.version = version;
    }

    public void avanzarA(EstadoDerivacion nuevoEstado) {
        if (!estado.puedeTransicionarA(nuevoEstado)) {
            throw new InterconsultaException("Transición inválida: " + estado + " -> " + nuevoEstado);
        }
        this.estado = nuevoEstado;
    }

    public String interconsultaId() { return interconsultaId; }
    public String pacienteId() { return pacienteId; }
    public String centroOrigenId() { return centroOrigenId; }
    public String especialidad() { return especialidad; }
    public EstadoDerivacion estado() { return estado; }
    public Instant creadaEn() { return creadaEn; }
    public long version() { return version; }
}
