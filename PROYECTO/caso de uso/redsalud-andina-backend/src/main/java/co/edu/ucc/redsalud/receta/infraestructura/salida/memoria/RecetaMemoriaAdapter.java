package co.edu.ucc.redsalud.receta.infraestructura.salida.memoria;

import co.edu.ucc.redsalud.receta.aplicacion.puertos.salida.DespachoFarmaciaPort;
import co.edu.ucc.redsalud.receta.aplicacion.puertos.salida.FirmadorKmsPort;
import co.edu.ucc.redsalud.receta.aplicacion.puertos.salida.RecetaRepository;
import co.edu.ucc.redsalud.receta.dominio.FirmaKms;
import co.edu.ucc.redsalud.receta.dominio.RecetaException;
import co.edu.ucc.redsalud.receta.dominio.RecetaMedica;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RecetaMemoriaAdapter implements RecetaRepository, FirmadorKmsPort, DespachoFarmaciaPort {

    private final Map<String, RecetaMedica> recetas = new ConcurrentHashMap<>();
    private final List<String> despachadas = new CopyOnWriteArrayList<>();
    private final String kmsKeyId;

    public RecetaMemoriaAdapter(String kmsKeyId) {
        this.kmsKeyId = kmsKeyId;
    }

    @Override
    public Optional<RecetaMedica> buscarPorId(String id) {
        return Optional.ofNullable(recetas.get(id));
    }

    @Override
    public RecetaMedica guardar(RecetaMedica receta) {
        recetas.put(receta.recetaId(), receta);
        return receta;
    }

    @Override
    public FirmaKms firmar(String contenido) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(contenido.getBytes(StandardCharsets.UTF_8));
            return new FirmaKms(HexFormat.of().formatHex(digest), kmsKeyId, Instant.now());
        } catch (NoSuchAlgorithmException ex) {
            throw new RecetaException("Algoritmo SHA-256 no disponible");
        }
    }

    @Override
    public void publicarEnColaFarmacia(RecetaMedica receta) {
        despachadas.add(receta.recetaId());
    }

    public List<String> despachadas() {
        return List.copyOf(despachadas);
    }
}
