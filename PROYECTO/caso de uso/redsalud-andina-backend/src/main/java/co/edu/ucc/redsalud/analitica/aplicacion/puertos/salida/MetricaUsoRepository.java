package co.edu.ucc.redsalud.analitica.aplicacion.puertos.salida;

import co.edu.ucc.redsalud.analitica.dominio.MetricaCentro;
import java.util.List;

public interface MetricaUsoRepository {

    MetricaCentro guardar(MetricaCentro metrica);

    List<MetricaCentro> listarTodas();
}
