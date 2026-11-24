package co.edu.ucc.redsalud.legado.infraestructura.salida.soap;

public record SoapHistorialDto(
        String codPacHist,
        String xmlResumenAnt,
        String campoAdminDescartado,
        String fecUltCargaNoct) {
}
