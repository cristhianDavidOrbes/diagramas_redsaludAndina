package co.edu.ucc.redsalud.identidad.dominio;

import java.util.Objects;
import java.util.Set;

public record PrincipalClinico(
        String usuarioId,
        RolUsuario rol,
        String centroId,
        String especialidad,
        Set<String> pacientesAsignados,
        boolean activo) {

    public PrincipalClinico {
        Objects.requireNonNull(usuarioId, "usuarioId obligatorio");
        Objects.requireNonNull(rol, "rol obligatorio");
        Objects.requireNonNull(centroId, "centroId obligatorio");
        pacientesAsignados = pacientesAsignados == null ? Set.of() : Set.copyOf(pacientesAsignados);
    }
}
