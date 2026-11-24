package co.edu.ucc.redsalud.interconsulta.infraestructura.salida.memoria;

import co.edu.ucc.redsalud.interconsulta.aplicacion.puertos.salida.InterconsultaRepository;
import co.edu.ucc.redsalud.interconsulta.dominio.Interconsulta;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InterconsultaMemoriaAdapter implements InterconsultaRepository {

    private final Map<String, Interconsulta> datos = new ConcurrentHashMap<>();

    @Override
    public Optional<Interconsulta> buscarPorId(String id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public Interconsulta guardar(Interconsulta interconsulta) {
        datos.put(interconsulta.interconsultaId(), interconsulta);
        return interconsulta;
    }
}
