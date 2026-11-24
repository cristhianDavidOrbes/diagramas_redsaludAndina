package co.edu.ucc.redsalud.analitica.aplicacion.servicios;

import co.edu.ucc.redsalud.analitica.aplicacion.puertos.entrada.ConsultarIndicadoresUseCase;
import co.edu.ucc.redsalud.analitica.aplicacion.puertos.salida.MetricaUsoRepository;
import co.edu.ucc.redsalud.analitica.dominio.MetricaCentro;
import co.edu.ucc.redsalud.analitica.dominio.TableroAdopcion;
import java.util.List;

public class ConsultarIndicadoresService implements ConsultarIndicadoresUseCase {

    private final MetricaUsoRepository repositorio;

    public ConsultarIndicadoresService(MetricaUsoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public MetricaCentro registrar(MetricaCentro metrica) {
        return repositorio.guardar(metrica);
    }

    @Override
    public TableroAdopcion obtenerTablero() {
        List<MetricaCentro> centros = repositorio.listarTodas();
        double promedio = centros.stream().mapToDouble(MetricaCentro::disponibilidadPct).average().orElse(100.0);
        boolean slo = centros.stream().allMatch(MetricaCentro::cumpleSlo);
        return new TableroAdopcion(centros, promedio, slo);
    }
}
