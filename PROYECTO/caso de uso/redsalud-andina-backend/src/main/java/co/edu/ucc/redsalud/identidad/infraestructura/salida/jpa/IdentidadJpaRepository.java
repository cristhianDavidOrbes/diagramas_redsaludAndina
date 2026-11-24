package co.edu.ucc.redsalud.identidad.infraestructura.salida.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IdentidadJpaRepository extends JpaRepository<UsuarioClinicoJpaEntity, String> {
}
