package co.edu.ucc.redsalud.sincronizacion.infraestructura.salida.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaveIdempotenciaJpaRepository extends JpaRepository<ClaveIdempotenciaJpaEntity, String> {
}
