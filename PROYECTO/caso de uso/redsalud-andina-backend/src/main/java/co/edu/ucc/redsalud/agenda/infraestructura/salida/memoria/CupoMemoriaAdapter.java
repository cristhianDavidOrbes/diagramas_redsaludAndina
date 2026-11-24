package co.edu.ucc.redsalud.agenda.infraestructura.salida.memoria;

import co.edu.ucc.redsalud.agenda.aplicacion.puertos.salida.CupoRepository;
import co.edu.ucc.redsalud.agenda.dominio.CupoEspecialista;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CupoMemoriaAdapter implements CupoRepository {

    private final Map<String, CupoEspecialista> cupos = new ConcurrentHashMap<>();

    @Override
    public Optional<CupoEspecialista> buscarPorId(String cupoId) {
        return Optional.ofNullable(cupos.get(cupoId));
    }

    @Override
    public CupoEspecialista guardar(CupoEspecialista cupo) {
        cupos.put(cupo.cupoId(), cupo);
        return cupo;
    }
}
