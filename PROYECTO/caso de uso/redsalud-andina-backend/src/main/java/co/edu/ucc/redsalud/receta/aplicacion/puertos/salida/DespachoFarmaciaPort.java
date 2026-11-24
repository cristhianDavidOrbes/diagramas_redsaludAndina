package co.edu.ucc.redsalud.receta.aplicacion.puertos.salida;

import co.edu.ucc.redsalud.receta.dominio.RecetaMedica;

public interface DespachoFarmaciaPort {

    void publicarEnColaFarmacia(RecetaMedica receta);
}
