package co.edu.ucc.redsalud.sincronizacion.infraestructura.salida.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConflictoLwwJpaRepository extends JpaRepository<ConflictoLwwJpaEntity, String> {

    List<ConflictoLwwJpaEntity> findByAgregadoId(String agregadoId);
}
