package co.edu.ucc.redsalud.receta.infraestructura.config;

import co.edu.ucc.redsalud.receta.aplicacion.puertos.entrada.EmitirRecetaUseCase;
import co.edu.ucc.redsalud.receta.aplicacion.puertos.salida.DespachoFarmaciaPort;
import co.edu.ucc.redsalud.receta.aplicacion.puertos.salida.FirmadorKmsPort;
import co.edu.ucc.redsalud.receta.aplicacion.puertos.salida.RecetaRepository;
import co.edu.ucc.redsalud.receta.aplicacion.servicios.EmitirRecetaService;
import co.edu.ucc.redsalud.receta.infraestructura.salida.aws.SqsFarmaciaAdapter;
import co.edu.ucc.redsalud.receta.infraestructura.salida.jpa.RecetaJpaAdapter;
import co.edu.ucc.redsalud.receta.infraestructura.salida.jpa.RecetaJpaRepository;
import co.edu.ucc.redsalud.receta.infraestructura.salida.memoria.RecetaMemoriaAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class RecetaConfig {

    @Bean
    @Profile("esqueleto")
    public RecetaMemoriaAdapter recetaMemoriaAdapter(@Value("${redsalud.aws.kms-key-id}") String kmsKeyId) {
        return new RecetaMemoriaAdapter(kmsKeyId);
    }

    @Bean
    @Profile("postgres")
    public RecetaRepository recetaJpaRepositoryAdapter(RecetaJpaRepository jpa) {
        return new RecetaJpaAdapter(jpa);
    }

    @Bean
    @Profile("postgres")
    public SqsFarmaciaAdapter sqsFarmaciaAdapter(
            SqsAsyncClient sqs,
            @Value("${redsalud.aws.sqs-recetas-fifo}") String queueUrl,
            @Value("${redsalud.aws.kms-key-id}") String kmsKeyId) {
        return new SqsFarmaciaAdapter(sqs, queueUrl, kmsKeyId);
    }

    @Bean
    public EmitirRecetaUseCase emitirRecetaUseCase(
            RecetaRepository repo, FirmadorKmsPort firmador, DespachoFarmaciaPort farmacia) {
        return new EmitirRecetaService(repo, firmador, farmacia);
    }
}
