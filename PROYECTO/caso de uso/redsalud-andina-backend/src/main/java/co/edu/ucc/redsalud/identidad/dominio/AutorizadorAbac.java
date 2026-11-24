package co.edu.ucc.redsalud.identidad.dominio;

import java.time.Instant;

public class AutorizadorAbac {

    public DecisionAcceso evaluar(PrincipalClinico p, String pacienteId, String centroRecurso,
                                  String recursoId, String motivo) {
        Instant ahora = Instant.now();
        if (p == null || !p.activo()) {
            return new DecisionAcceso(false, p == null ? "DESCONOCIDO" : p.usuarioId(),
                    recursoId, motivo, "Usuario inactivo o inexistente", ahora);
        }
        if (!p.rol().esClinico()) {
            return new DecisionAcceso(false, p.usuarioId(), recursoId, motivo,
                    "Rol sin privilegio clínico (ASR-5): " + p.rol(), ahora);
        }
        if (motivo == null || motivo.isBlank()) {
            return new DecisionAcceso(false, p.usuarioId(), recursoId, "", "Motivo clínico obligatorio", ahora);
        }
        boolean relacionVigente = p.centroId().equals(centroRecurso)
                || p.pacientesAsignados().contains(pacienteId);
        if (!relacionVigente) {
            return new DecisionAcceso(false, p.usuarioId(), recursoId, motivo,
                    "Sin relación vigente por centro ni asignación con paciente", ahora);
        }
        return new DecisionAcceso(true, p.usuarioId(), recursoId, motivo, "Autorizado por ABAC", ahora);
    }
}
