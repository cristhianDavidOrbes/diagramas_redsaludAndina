package co.edu.ucc.redsalud.legado.infraestructura.config;

import co.edu.ucc.redsalud.legado.aplicacion.puertos.entrada.ConsultarHistorialUseCase;
import co.edu.ucc.redsalud.legado.aplicacion.puertos.salida.HistorialClinicoPort;
import co.edu.ucc.redsalud.legado.aplicacion.puertos.salida.ReadModelCachePort;
import co.edu.ucc.redsalud.legado.aplicacion.servicios.ConsultarHistorialService;
import co.edu.ucc.redsalud.legado.infraestructura.salida.cache.CacheTwoLevelAdapter;
import co.edu.ucc.redsalud.legado.infraestructura.salida.memoria.LegadoMemoriaAdapter;
import co.edu.ucc.redsalud.legado.infraestructura.salida.soap.AdaptadorSoapResiliente;
import co.edu.ucc.redsalud.legado.infraestructura.salida.soap.TraductorLegadoSoap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.time.Duration;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;

@Configuration
public class LegadoConfig {

    @Bean
    public ReadModelCachePort readModelCachePort(ObjectProvider<StringRedisTemplate> redis) {
        return new CacheTwoLevelAdapter(redis.getIfAvailable());
    }

    @Bean
    public TraductorLegadoSoap traductorLegadoSoap() {
        return new TraductorLegadoSoap();
    }

    @Bean
    @Profile("esqueleto")
    public LegadoMemoriaAdapter legadoMemoriaAdapter() {
        return new LegadoMemoriaAdapter();
    }

    @Bean
    @Profile("postgres")
    public HistorialClinicoPort adaptadorSoapResiliente(
            TraductorLegadoSoap traductor,
            @Value("${redsalud.legado.soap-uri}") String soapUri) {
        HttpUrlConnectionMessageSender sender = new HttpUrlConnectionMessageSender();
        sender.setConnectionTimeout(Duration.ofSeconds(3));
        sender.setReadTimeout(Duration.ofSeconds(5));
        WebServiceTemplate ws = new WebServiceTemplate();
        ws.setMessageSender(sender);
        return new AdaptadorSoapResiliente(ws, traductor, soapUri);
    }

    @Bean
    public ConsultarHistorialUseCase consultarHistorialUseCase(
            ReadModelCachePort cache, HistorialClinicoPort legadoPort,
            @Value("${redsalud.legado.cache-ttl-segundos}") long ttlSegundos) {
        return new ConsultarHistorialService(cache, legadoPort, ttlSegundos);
    }
}
