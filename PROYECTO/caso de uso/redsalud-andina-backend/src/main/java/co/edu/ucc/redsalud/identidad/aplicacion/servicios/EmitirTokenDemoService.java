package co.edu.ucc.redsalud.identidad.aplicacion.servicios;

import co.edu.ucc.redsalud.identidad.aplicacion.puertos.entrada.EmitirTokenDemoUseCase;
import co.edu.ucc.redsalud.identidad.aplicacion.puertos.salida.EmisorTokenPort;
import co.edu.ucc.redsalud.identidad.aplicacion.puertos.salida.IdentidadRepository;
import co.edu.ucc.redsalud.identidad.dominio.IdentidadException;
import co.edu.ucc.redsalud.identidad.dominio.PrincipalClinico;

public class EmitirTokenDemoService implements EmitirTokenDemoUseCase {

    private final IdentidadRepository repositorio;
    private final EmisorTokenPort emisor;

    public EmitirTokenDemoService(IdentidadRepository repositorio, EmisorTokenPort emisor) {
        this.repositorio = repositorio;
        this.emisor = emisor;
    }

    @Override
    public String emitirToken(String usuarioId) {
        PrincipalClinico principal = repositorio.buscarPorId(usuarioId)
                .orElseThrow(() -> new IdentidadException("Usuario no encontrado para emitir JWT: " + usuarioId));
        return emisor.firmarJwt(principal);
    }
}
