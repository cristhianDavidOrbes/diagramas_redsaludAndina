package co.edu.ucc.redsalud.auditoria.dominio;

import java.time.Instant;
import java.util.List;

public record ReporteAuditoria(
        String recursoId,
        Instant generadoEn,
        boolean cadenaIntegra,
        long accesosPermitidos,
        long accesosDenegados,
        List<TrazaAccesoClinico> trazas) {

    public String aMarkdown() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Reporte de Auditoría Clínica: ").append(recursoId).append("\n\n")
          .append("- **Integridad cadena SHA-256:** ").append(cadenaIntegra ? "VERIFICADA" : "ALTERADA").append("\n")
          .append("- **Permitidos:** ").append(accesosPermitidos)
          .append(" · **Denegados:** ").append(accesosDenegados).append("\n\n")
          .append("| Fecha | Usuario | Rol | Centro | Motivo | Resultado |\n")
          .append("| --- | --- | --- | --- | --- | --- |\n");
        for (TrazaAccesoClinico t : trazas) {
            sb.append("| ").append(t.ocurridoEn()).append(" | ").append(t.usuarioId())
              .append(" | ").append(t.rol()).append(" | ").append(t.centroId())
              .append(" | ").append(t.motivo()).append(" | ")
              .append(t.permitido() ? "PERMITIDO" : "DENEGADO").append(" |\n");
        }
        return sb.toString();
    }
}
