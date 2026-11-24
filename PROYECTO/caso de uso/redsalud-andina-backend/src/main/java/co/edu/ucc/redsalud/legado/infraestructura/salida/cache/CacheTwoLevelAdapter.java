package co.edu.ucc.redsalud.legado.infraestructura.salida.cache;

import co.edu.ucc.redsalud.legado.aplicacion.puertos.salida.ReadModelCachePort;
import co.edu.ucc.redsalud.legado.dominio.HistorialCanonico;
import co.edu.ucc.redsalud.legado.dominio.MarcaFrescura;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;

public class CacheTwoLevelAdapter implements ReadModelCachePort {

    private final Cache<String, HistorialCanonico> l1;
    private final StringRedisTemplate redisL2;

    public CacheTwoLevelAdapter(StringRedisTemplate redisL2) {
        this.redisL2 = redisL2;
        this.l1 = Caffeine.newBuilder().maximumSize(1_000).recordStats().build();
    }

    @Override
    public Optional<HistorialCanonico> buscar(String pacienteId) {
        HistorialCanonico hitL1 = l1.getIfPresent(pacienteId);
        if (hitL1 != null) {
            return Optional.of(hitL1);
        }
        if (redisL2 == null) {
            return Optional.empty();
        }
        String raw = redisL2.opsForValue().get("historial:" + pacienteId);
        if (raw == null) {
            return Optional.empty();
        }
        String[] partes = raw.split("\\|", 2);
        HistorialCanonico hitL2 = new HistorialCanonico(
                pacienteId, partes[1], Instant.parse(partes[0]), MarcaFrescura.FRESH, false);
        l1.put(pacienteId, hitL2);
        return Optional.of(hitL2);
    }

    @Override
    public void guardar(HistorialCanonico h) {
        l1.put(h.pacienteId(), h);
        if (redisL2 != null) {
            redisL2.opsForValue().set(
                    "historial:" + h.pacienteId(),
                    h.fechaSincronizacion() + "|" + h.resumenAntecedentes(),
                    Duration.ofHours(24));
        }
    }
}
