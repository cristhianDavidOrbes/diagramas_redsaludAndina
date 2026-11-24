package co.edu.ucc.redsalud.identidad.dominio;

public enum RolUsuario {
    MEDICO_RURAL(true),
    ESPECIALISTA(true),
    AUDITOR(false),
    ADMIN(false);

    private final boolean clinico;

    RolUsuario(boolean clinico) {
        this.clinico = clinico;
    }

    public boolean esClinico() {
        return clinico;
    }
}
