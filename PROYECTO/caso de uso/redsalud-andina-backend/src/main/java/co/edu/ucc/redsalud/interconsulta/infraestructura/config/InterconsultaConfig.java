package co.edu.ucc.redsalud.interconsulta.infraestructura.config;

import co.edu.ucc.redsalud.interconsulta.aplicacion.puertos.entrada.GestionarInterconsultaUseCase;
import co.edu.ucc.redsalud.interconsulta.aplicacion.puertos.salida.InterconsultaRepository;
import co.edu.ucc.redsalud.interconsulta.aplicacion.servicios.GestionarInterconsultaService;
import co.edu.ucc.redsalud.interconsulta.infraestructura.salida.jpa.InterconsultaJpaAdapter;
import co.edu.ucc.redsalud.interconsulta.infraestructura.salida.jpa.InterconsultaJpaRepository;
import co.edu.ucc.redsalud.interconsulta.infraestructura.salida.memoria.InterconsultaMemoriaAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class InterconsultaConfig {

    @Bean
    @Profile("esqueleto")
    public InterconsultaRepository interconsultaMemoriaRepository() {
        return new InterconsultaMemoriaAdapter();
    }

    @Bean
    @Profile("postgres")
    public InterconsultaRepository interconsultaJpaRepositoryAdapter(InterconsultaJpaRepository jpa) {
        return new InterconsultaJpaAdapter(jpa);
    }

    @Bean
    public GestionarInterconsultaUseCase gestionarInterconsultaUseCase(InterconsultaRepository repo) {
        return new GestionarInterconsultaService(repo);
    }
}
