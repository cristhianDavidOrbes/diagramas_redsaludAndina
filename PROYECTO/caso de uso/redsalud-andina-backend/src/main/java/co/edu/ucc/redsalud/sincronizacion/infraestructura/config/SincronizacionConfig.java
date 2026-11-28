package co.edu.ucc.redsalud.sincronizacion.infraestructura.config;

import co.edu.ucc.redsalud.sincronizacion.aplicacion.puertos.entrada.SincronizarEpisodiosUseCase;
import co.edu.ucc.redsalud.sincronizacion.aplicacion.puertos.salida.IdempotenciaRepository;
import co.edu.ucc.redsalud.sincronizacion.aplicacion.puertos.salida.OutboxRepository;
import co.edu.ucc.redsalud.sincronizacion.aplicacion.puertos.salida.PublicadorOutboxPort;
import co.edu.ucc.redsalud.sincronizacion.aplicacion.servicios.SincronizarEpisodiosService;
import co.edu.ucc.redsalud.sincronizacion.dominio.LoteSincronizacion;
import co.edu.ucc.redsalud.sincronizacion.dominio.ResultadoSincronizacion;
import co.edu.ucc.redsalud.sincronizacion.infraestructura.salida.aws.SqsFifoOutboxAdapter;
import co.edu.ucc.redsalud.sincronizacion.infraestructura.salida.jpa.ClaveIdempotenciaJpaRepository;
import co.edu.ucc.redsalud.sincronizacion.infraestructura.salida.jpa.ConflictoLwwJpaRepository;
import co.edu.ucc.redsalud.sincronizacion.infraestructura.salida.jpa.OutboxEventoJpaRepository;
import co.edu.ucc.redsalud.sincronizacion.infraestructura.salida.jpa.SincronizacionJpaAdapter;
import co.edu.ucc.redsalud.sincronizacion.infraestructura.salida.memoria.SincronizacionMemoriaAdapter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class SincronizacionConfig {

    @Bean
    @Profile("esqueleto")
    public SincronizacionMemoriaAdapter sincronizacionMemoriaAdapter() {
        return new SincronizacionMemoriaAdapter();
    }

    @Bean
    @Profile("postgres")
    public SincronizacionJpaAdapter sincronizacionJpaAdapter(
            ClaveIdempotenciaJpaRepository claves, OutboxEventoJpaRepository outbox, ConflictoLwwJpaRepository conflictos) {
        return new SincronizacionJpaAdapter(claves, outbox, conflictos);
    }

    @Bean
    @Profile("postgres")
    public PublicadorOutboxPort sqsFifoOutboxAdapter(
            ObjectProvider<SqsAsyncClient> sqsProvider, @Value("${redsalud.aws.sqs-sync-fifo}") String queueUrl) {
        SqsAsyncClient sqs = sqsProvider.getIfAvailable();
        return sqs != null ? new SqsFifoOutboxAdapter(sqs, queueUrl) : new SincronizacionMemoriaAdapter();
    }

    @Bean
    public SincronizarEpisodiosUseCase sincronizarEpisodiosUseCase(
            IdempotenciaRepository idempotencia, OutboxRepository outbox, PublicadorOutboxPort publicador,
            ObjectProvider<PlatformTransactionManager> txManager) {
        SincronizarEpisodiosService servicio = new SincronizarEpisodiosService(idempotencia, outbox, publicador);
        PlatformTransactionManager tm = txManager.getIfAvailable();
        if (tm == null) {
            return servicio;
        }
        TransactionTemplate tx = new TransactionTemplate(tm);
        return new SincronizarEpisodiosUseCase() {
            @Override
            public ResultadoSincronizacion sincronizar(LoteSincronizacion lote) {
                return tx.execute(s -> servicio.sincronizar(lote));
            }
            @Override
            public int drenarOutboxPendiente() {
                return tx.execute(s -> servicio.drenarOutboxPendiente());
            }
        };
    }
}
