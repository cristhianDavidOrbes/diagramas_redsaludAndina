package co.edu.ucc.redsalud.analitica.infraestructura.config;

import co.edu.ucc.redsalud.analitica.aplicacion.puertos.entrada.ConsultarIndicadoresUseCase;
import co.edu.ucc.redsalud.analitica.aplicacion.puertos.salida.MetricaUsoRepository;
import co.edu.ucc.redsalud.analitica.aplicacion.servicios.ConsultarIndicadoresService;
import co.edu.ucc.redsalud.analitica.infraestructura.salida.metricas.MicrometerAnaliticaAdapter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnaliticaConfig {

    @Bean
    public MetricaUsoRepository metricaUsoRepository(MeterRegistry registry) {
        return new MicrometerAnaliticaAdapter(registry);
    }

    @Bean
    public ConsultarIndicadoresUseCase consultarIndicadoresUseCase(MetricaUsoRepository repo) {
        return new ConsultarIndicadoresService(repo);
    }
}
