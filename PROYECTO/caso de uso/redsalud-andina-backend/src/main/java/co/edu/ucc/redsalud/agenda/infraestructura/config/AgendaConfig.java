package co.edu.ucc.redsalud.agenda.infraestructura.config;

import co.edu.ucc.redsalud.agenda.aplicacion.puertos.entrada.ReservarCupoUseCase;
import co.edu.ucc.redsalud.agenda.aplicacion.puertos.salida.CupoRepository;
import co.edu.ucc.redsalud.agenda.aplicacion.servicios.ReservarCupoService;
import co.edu.ucc.redsalud.agenda.infraestructura.salida.jpa.CupoJpaAdapter;
import co.edu.ucc.redsalud.agenda.infraestructura.salida.jpa.CupoJpaRepository;
import co.edu.ucc.redsalud.agenda.infraestructura.salida.memoria.CupoMemoriaAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class AgendaConfig {

    @Bean
    @Profile("esqueleto")
    public CupoRepository cupoMemoriaRepository() {
        return new CupoMemoriaAdapter();
    }

    @Bean
    @Profile("postgres")
    public CupoRepository cupoJpaRepositoryAdapter(CupoJpaRepository jpa) {
        return new CupoJpaAdapter(jpa);
    }

    @Bean
    public ReservarCupoUseCase reservarCupoUseCase(CupoRepository repositorio) {
        return new ReservarCupoService(repositorio);
    }
}
