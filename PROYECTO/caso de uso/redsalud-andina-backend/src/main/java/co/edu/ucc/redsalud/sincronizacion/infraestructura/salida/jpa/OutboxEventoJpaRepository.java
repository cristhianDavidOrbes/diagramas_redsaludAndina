package co.edu.ucc.redsalud.sincronizacion.infraestructura.salida.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventoJpaRepository extends JpaRepository<OutboxEventoJpaEntity, String> {

    List<OutboxEventoJpaEntity> findByPublicadoFalseOrderByCreadoEnAsc();
}
