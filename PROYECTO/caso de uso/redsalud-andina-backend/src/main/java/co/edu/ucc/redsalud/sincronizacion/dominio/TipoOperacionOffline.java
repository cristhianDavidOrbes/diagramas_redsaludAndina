package co.edu.ucc.redsalud.sincronizacion.dominio;

public enum TipoOperacionOffline {
    NOTA_EVOLUCION(true),
    SIGNOS_VITALES(true),
    ADJUNTO_EXAMEN(true),
    RESERVA_CUPO(false),
    RECETA_FIRMADA(false);

    private final boolean permitidaEnBorde;

    TipoOperacionOffline(boolean permitidaEnBorde) {
        this.permitidaEnBorde = permitidaEnBorde;
    }

    public boolean permitidaEnBorde() {
        return permitidaEnBorde;
    }
}
