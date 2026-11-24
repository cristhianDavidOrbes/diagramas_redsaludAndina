package co.edu.ucc.redsalud.identidad.infraestructura.salida.jpa;

import co.edu.ucc.redsalud.identidad.aplicacion.puertos.salida.IdentidadRepository;
import co.edu.ucc.redsalud.identidad.aplicacion.puertos.salida.RegistroDenegacionPort;
import co.edu.ucc.redsalud.identidad.dominio.DecisionAcceso;
import co.edu.ucc.redsalud.identidad.dominio.PrincipalClinico;
import co.edu.ucc.redsalud.identidad.dominio.RolUsuario;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class IdentidadJpaAdapter implements IdentidadRepository, RegistroDenegacionPort {

    private final IdentidadJpaRepository jpa;
    private final List<DecisionAcceso> decisiones = new CopyOnWriteArrayList<>();

    public IdentidadJpaAdapter(IdentidadJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<PrincipalClinico> buscarPorId(String id) {
        return jpa.findById(id).map(e -> new PrincipalClinico(
                e.getUsuarioId(), RolUsuario.valueOf(e.getRol()), e.getCentroId(),
                e.getEspecialidad(), Set.of(), e.isActivo()));
    }

    @Override
    public PrincipalClinico guardar(PrincipalClinico p) {
        jpa.save(new UsuarioClinicoJpaEntity(p.usuarioId(), p.rol().name(), p.centroId(), p.especialidad(), p.activo(), 0L));
        return p;
    }

    @Override
    public void registrarDecision(DecisionAcceso d) { decisiones.add(d); }

    @Override
    public List<DecisionAcceso> listarDenegaciones() {
        return decisiones.stream().filter(d -> !d.permitido()).toList();
    }
}
