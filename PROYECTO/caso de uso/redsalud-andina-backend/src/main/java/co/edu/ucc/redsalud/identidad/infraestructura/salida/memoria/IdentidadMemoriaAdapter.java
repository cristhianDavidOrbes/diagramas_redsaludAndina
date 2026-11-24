package co.edu.ucc.redsalud.identidad.infraestructura.salida.memoria;

import co.edu.ucc.redsalud.identidad.aplicacion.puertos.salida.IdentidadRepository;
import co.edu.ucc.redsalud.identidad.aplicacion.puertos.salida.RegistroDenegacionPort;
import co.edu.ucc.redsalud.identidad.dominio.DecisionAcceso;
import co.edu.ucc.redsalud.identidad.dominio.PrincipalClinico;
import co.edu.ucc.redsalud.identidad.dominio.RolUsuario;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class IdentidadMemoriaAdapter implements IdentidadRepository, RegistroDenegacionPort {

    private final Map<String, PrincipalClinico> usuarios = new ConcurrentHashMap<>();
    private final List<DecisionAcceso> decisiones = new CopyOnWriteArrayList<>();

    public IdentidadMemoriaAdapter() {
        guardar(new PrincipalClinico("MED-RURAL-1", RolUsuario.MEDICO_RURAL, "CENTRO-01", "GENERAL", Set.of("PAC-001"), true));
        guardar(new PrincipalClinico("ESP-CARD-1", RolUsuario.ESPECIALISTA, "CENTRO-02", "CARDIOLOGIA", Set.of("PAC-001"), true));
        guardar(new PrincipalClinico("ADMIN-1", RolUsuario.ADMIN, "CENTRO-01", "NINGUNA", Set.of(), true));
    }

    @Override
    public Optional<PrincipalClinico> buscarPorId(String id) { return Optional.ofNullable(usuarios.get(id)); }
    @Override
    public PrincipalClinico guardar(PrincipalClinico p) { usuarios.put(p.usuarioId(), p); return p; }
    @Override
    public void registrarDecision(DecisionAcceso d) { decisiones.add(d); }
    @Override
    public List<DecisionAcceso> listarDenegaciones() {
        return decisiones.stream().filter(d -> !d.permitido()).toList();
    }
}
