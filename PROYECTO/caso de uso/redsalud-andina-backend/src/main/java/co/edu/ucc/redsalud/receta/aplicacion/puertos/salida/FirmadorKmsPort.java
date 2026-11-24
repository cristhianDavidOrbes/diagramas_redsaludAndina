package co.edu.ucc.redsalud.receta.aplicacion.puertos.salida;

import co.edu.ucc.redsalud.receta.dominio.FirmaKms;

public interface FirmadorKmsPort {

    FirmaKms firmar(String contenidoCanonicamenteSerializado);
}
