package co.edu.ucc.redsalud.identidad.infraestructura.salida.oidc;

import co.edu.ucc.redsalud.identidad.dominio.PrincipalClinico;
import co.edu.ucc.redsalud.identidad.dominio.RolUsuario;
import java.util.List;
import java.util.Set;
import org.springframework.security.oauth2.jwt.Jwt;

public class TraductorJwtOidc {

    public PrincipalClinico desdeJwt(Jwt jwt) {
        String rolClaim = jwt.getClaimAsString("rol");
        RolUsuario rol = rolClaim != null ? RolUsuario.valueOf(rolClaim) : RolUsuario.ADMIN;
        String centro = jwt.getClaimAsString("centro_id");
        String especialidad = jwt.getClaimAsString("especialidad");
        List<String> pacientes = jwt.getClaimAsStringList("pacientes");
        return new PrincipalClinico(
                jwt.getSubject(),
                rol,
                centro != null ? centro : "SIN_CENTRO",
                especialidad != null ? especialidad : "GENERAL",
                pacientes != null ? Set.copyOf(pacientes) : Set.of(),
                true);
    }
}
