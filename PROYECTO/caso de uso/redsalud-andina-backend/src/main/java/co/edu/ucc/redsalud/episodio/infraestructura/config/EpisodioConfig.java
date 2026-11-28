package co.edu.ucc.redsalud.episodio.infraestructura.config;

import co.edu.ucc.redsalud.episodio.aplicacion.puertos.entrada.RegistrarEpisodioUseCase;
import co.edu.ucc.redsalud.episodio.aplicacion.puertos.salida.AdjuntoStoragePort;
import co.edu.ucc.redsalud.episodio.aplicacion.puertos.salida.EpisodioRepository;
import co.edu.ucc.redsalud.episodio.aplicacion.servicios.RegistrarEpisodioService;
import co.edu.ucc.redsalud.episodio.infraestructura.salida.aws.S3AdjuntoKmsAdapter;
import co.edu.ucc.redsalud.episodio.infraestructura.salida.jpa.EpisodioJpaAdapter;
import co.edu.ucc.redsalud.episodio.infraestructura.salida.jpa.EpisodioJpaRepository;
import co.edu.ucc.redsalud.episodio.infraestructura.salida.memoria.EpisodioMemoriaAdapter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class EpisodioConfig {

    @Bean
    @Profile("esqueleto")
    public EpisodioMemoriaAdapter episodioMemoriaAdapter(
            @Value("${redsalud.aws.kms-key-id}") String kmsKeyId,
            @Value("${redsalud.region-permitida}") String region) {
        return new EpisodioMemoriaAdapter(kmsKeyId, region);
    }

    @Bean
    @Profile("postgres")
    public EpisodioRepository episodioJpaRepositoryAdapter(
            EpisodioJpaRepository jpa,
            @Value("${redsalud.aws.kms-key-id}") String kmsKeyId,
            @Value("${redsalud.region-permitida}") String region) {
        return new EpisodioJpaAdapter(jpa, kmsKeyId, region);
    }

    @Bean
    @Profile("postgres")
    public AdjuntoStoragePort s3AdjuntoStoragePort(
            ObjectProvider<S3Client> s3Provider,
            @Value("${redsalud.aws.s3-adjuntos-bucket}") String bucket,
            @Value("${redsalud.aws.kms-key-id}") String kmsKeyId,
            @Value("${redsalud.region-permitida}") String region) {
        S3Client s3 = s3Provider.getIfAvailable();
        return s3 != null ? new S3AdjuntoKmsAdapter(s3, bucket, kmsKeyId, region)
                : new EpisodioMemoriaAdapter(kmsKeyId, region);
    }

    @Bean
    public RegistrarEpisodioUseCase registrarEpisodioUseCase(
            EpisodioRepository repo, AdjuntoStoragePort storage,
            @Value("${redsalud.region-permitida}") String region) {
        return new RegistrarEpisodioService(repo, storage, region);
    }
}
