package co.edu.ucc.redsalud.analitica.aplicacion.puertos.entrada;

import co.edu.ucc.redsalud.analitica.dominio.MetricaCentro;
import co.edu.ucc.redsalud.analitica.dominio.TableroAdopcion;

public interface ConsultarIndicadoresUseCase {

    MetricaCentro registrar(MetricaCentro metrica);

    TableroAdopcion obtenerTablero();
}
