package co.edu.ucc.redsalud.episodio.infraestructura.salida.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EpisodioJpaRepository extends JpaRepository<EpisodioJpaEntity, String> {
}
