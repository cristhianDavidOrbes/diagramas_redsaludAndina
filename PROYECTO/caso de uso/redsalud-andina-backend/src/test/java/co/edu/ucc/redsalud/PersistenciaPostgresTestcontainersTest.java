package co.edu.ucc.redsalud;

import co.edu.ucc.redsalud.sincronizacion.aplicacion.servicios.SincronizarEpisodiosService;
import co.edu.ucc.redsalud.sincronizacion.dominio.ClaveIdempotencia;
import co.edu.ucc.redsalud.sincronizacion.dominio.LoteSincronizacion;
import co.edu.ucc.redsalud.sincronizacion.dominio.TipoOperacionOffline;
import co.edu.ucc.redsalud.sincronizacion.infraestructura.salida.jpa.ClaveIdempotenciaJpaRepository;
import co.edu.ucc.redsalud.sincronizacion.infraestructura.salida.jpa.ConflictoLwwJpaRepository;
import co.edu.ucc.redsalud.sincronizacion.infraestructura.salida.jpa.OutboxEventoJpaRepository;
import co.edu.ucc.redsalud.sincronizacion.infraestructura.salida.jpa.SincronizacionJpaAdapter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test-postgres")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class PersistenciaPostgresTestcontainersTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("redsalud")
            .withUsername("redsalud")
            .withPassword("redsalud_test");

    @DynamicPropertySource
    static void propiedadesPostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private ClaveIdempotenciaJpaRepository clavesRepo;

    @Autowired
    private OutboxEventoJpaRepository outboxRepo;

    @Autowired
    private ConflictoLwwJpaRepository conflictosRepo;

    @Test
    void flywayCreaEsquemasYPersisteIdempotenciaYOutboxEnPostgreSQLReal() {
        SincronizacionJpaAdapter jpaAdapter = new SincronizacionJpaAdapter(clavesRepo, outboxRepo, conflictosRepo);
        List<String> enviados = new ArrayList<>();
        SincronizarEpisodiosService service = new SincronizarEpisodiosService(
                jpaAdapter, jpaAdapter, evento -> enviados.add(evento.agregadoId()));

        LoteSincronizacion lote = new LoteSincronizacion(
                new ClaveIdempotencia("ULID-PG-001"), "EP-PG-1", TipoOperacionOffline.NOTA_EVOLUCION,
                "notaEvolucion", "Control rural persistido", Instant.now(), null, null);

        assertThat(service.sincronizar(lote).codigoHttp()).isEqualTo(201);
        assertThat(service.sincronizar(lote).codigoHttp()).isEqualTo(200);
        assertThat(clavesRepo.count()).isEqualTo(1L);
        assertThat(service.drenarOutboxPendiente()).isEqualTo(1);
        assertThat(enviados).containsExactly("EP-PG-1");
    }
}
