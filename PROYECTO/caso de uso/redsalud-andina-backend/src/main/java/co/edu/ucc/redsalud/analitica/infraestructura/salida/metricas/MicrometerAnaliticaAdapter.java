package co.edu.ucc.redsalud.analitica.infraestructura.salida.metricas;

import co.edu.ucc.redsalud.analitica.aplicacion.puertos.salida.MetricaUsoRepository;
import co.edu.ucc.redsalud.analitica.dominio.MetricaCentro;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MicrometerAnaliticaAdapter implements MetricaUsoRepository {

    private final Map<String, MetricaCentro> metricas = new ConcurrentHashMap<>();
    private final MeterRegistry registry;

    public MicrometerAnaliticaAdapter(MeterRegistry registry) {
        this.registry = registry;
        guardar(new MetricaCentro("CENTRO-01", 40L, 99.8, 4.2));
    }

    @Override
    public MetricaCentro guardar(MetricaCentro m) {
        metricas.put(m.centroId(), m);
        registry.counter("redsalud.teleconsultas.total", "centro", m.centroId()).increment(m.teleconsultas());
        return m;
    }

    @Override
    public List<MetricaCentro> listarTodas() {
        return List.copyOf(metricas.values());
    }
}
