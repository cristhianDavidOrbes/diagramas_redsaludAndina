package co.edu.ucc.redsalud.receta.dominio;

import java.util.Objects;

public class RecetaMedica {

    private final String recetaId;
    private final String episodioId;
    private final String pacienteId;
    private final String especialistaId;
    private final String medicamentos;
    private final FirmaKms firma;
    private boolean enviadaFarmacia;
    private final long version;

    public RecetaMedica(String recetaId, String episodioId, String pacienteId, String especialistaId,
                        String medicamentos, FirmaKms firma, boolean enviadaFarmacia, long version) {
        this.recetaId = Objects.requireNonNull(recetaId, "recetaId obligatorio");
        this.episodioId = Objects.requireNonNull(episodioId, "episodioId obligatorio");
        this.pacienteId = Objects.requireNonNull(pacienteId, "pacienteId obligatorio");
        this.especialistaId = Objects.requireNonNull(especialistaId, "especialistaId obligatorio");
        this.medicamentos = Objects.requireNonNull(medicamentos, "medicamentos obligatorios");
        this.firma = Objects.requireNonNull(firma, "Toda receta emitida debe llevar firma KMS");
        this.enviadaFarmacia = enviadaFarmacia;
        this.version = version;
    }

    public void intentarModificar(String nuevosMedicamentos) {
        throw new RecetaException("Receta firmada es inmutable; anule y emita una nueva: " + recetaId);
    }

    public void marcarEnviadaFarmacia() {
        this.enviadaFarmacia = true;
    }

    public String recetaId() { return recetaId; }
    public String episodioId() { return episodioId; }
    public String pacienteId() { return pacienteId; }
    public String especialistaId() { return especialistaId; }
    public String medicamentos() { return medicamentos; }
    public FirmaKms firma() { return firma; }
    public boolean enviadaFarmacia() { return enviadaFarmacia; }
    public long version() { return version; }
}
