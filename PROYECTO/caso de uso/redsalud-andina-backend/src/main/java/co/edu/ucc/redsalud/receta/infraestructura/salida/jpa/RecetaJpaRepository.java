package co.edu.ucc.redsalud.receta.infraestructura.salida.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RecetaJpaRepository extends JpaRepository<RecetaJpaEntity, String> {
}
