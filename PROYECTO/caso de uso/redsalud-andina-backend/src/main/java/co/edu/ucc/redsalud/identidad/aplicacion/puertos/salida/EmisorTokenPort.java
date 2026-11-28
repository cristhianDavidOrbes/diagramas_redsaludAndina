package co.edu.ucc.redsalud.identidad.aplicacion.puertos.salida;

import co.edu.ucc.redsalud.identidad.dominio.PrincipalClinico;

public interface EmisorTokenPort {

    String firmarJwt(PrincipalClinico principal);
}
