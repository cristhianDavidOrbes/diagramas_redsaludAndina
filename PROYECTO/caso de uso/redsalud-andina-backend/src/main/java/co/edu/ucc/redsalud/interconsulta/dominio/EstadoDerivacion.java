package co.edu.ucc.redsalud.interconsulta.dominio;

public enum EstadoDerivacion {
    SOLICITADA,
    AGENDADA,
    ATENDIDA,
    CERRADA;

    public boolean puedeTransicionarA(EstadoDerivacion destino) {
        return switch (this) {
            case SOLICITADA -> destino == AGENDADA;
            case AGENDADA -> destino == ATENDIDA;
            case ATENDIDA -> destino == CERRADA;
            case CERRADA -> false;
        };
    }
}
