package co.edu.ucc.redsalud.analitica.dominio;

import java.util.List;

public record TableroAdopcion(
        List<MetricaCentro> centros,
        double disponibilidadPromedio,
        boolean cumpleSloGlobal) {
}
