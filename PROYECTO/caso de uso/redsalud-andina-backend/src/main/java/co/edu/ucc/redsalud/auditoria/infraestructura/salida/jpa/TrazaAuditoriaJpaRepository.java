package co.edu.ucc.redsalud.auditoria.infraestructura.salida.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrazaAuditoriaJpaRepository extends JpaRepository<TrazaAuditoriaJpaEntity, String> {

    List<TrazaAuditoriaJpaEntity> findByRecursoIdOrderByOcurridoEnAsc(String recursoId);

    List<TrazaAuditoriaJpaEntity> findAllByOrderByOcurridoEnAsc();
}
