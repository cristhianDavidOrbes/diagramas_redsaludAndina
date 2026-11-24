package co.edu.ucc.redsalud.receta.aplicacion.servicios;

import co.edu.ucc.redsalud.receta.aplicacion.puertos.entrada.EmitirRecetaUseCase;
import co.edu.ucc.redsalud.receta.aplicacion.puertos.salida.DespachoFarmaciaPort;
import co.edu.ucc.redsalud.receta.aplicacion.puertos.salida.FirmadorKmsPort;
import co.edu.ucc.redsalud.receta.aplicacion.puertos.salida.RecetaRepository;
import co.edu.ucc.redsalud.receta.dominio.FirmaKms;
import co.edu.ucc.redsalud.receta.dominio.RecetaMedica;

public class EmitirRecetaService implements EmitirRecetaUseCase {

    private final RecetaRepository repositorio;
    private final FirmadorKmsPort firmador;
    private final DespachoFarmaciaPort farmacia;

    public EmitirRecetaService(RecetaRepository repositorio, FirmadorKmsPort firmador, DespachoFarmaciaPort farmacia) {
        this.repositorio = repositorio;
        this.firmador = firmador;
        this.farmacia = farmacia;
    }

    @Override
    public RecetaMedica emitir(String recetaId, String episodioId, String pacienteId,
                               String especialistaId, String medicamentos) {
        FirmaKms firma = firmador.firmar(recetaId + "|" + pacienteId + "|" + medicamentos);
        RecetaMedica receta = new RecetaMedica(
                recetaId, episodioId, pacienteId, especialistaId, medicamentos, firma, false, 0L);
        RecetaMedica guardada = repositorio.guardar(receta);
        farmacia.publicarEnColaFarmacia(guardada);
        guardada.marcarEnviadaFarmacia();
        return repositorio.guardar(guardada);
    }
}
