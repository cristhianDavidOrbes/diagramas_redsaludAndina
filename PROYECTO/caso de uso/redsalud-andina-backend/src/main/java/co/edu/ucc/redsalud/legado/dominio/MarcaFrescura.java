package co.edu.ucc.redsalud.legado.dominio;

public enum MarcaFrescura {
    LIVE("live"),
    FRESH("fresh"),
    STALE("stale");

    private final String etiqueta;

    MarcaFrescura(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String valorCabecera() {
        return etiqueta;
    }
}
