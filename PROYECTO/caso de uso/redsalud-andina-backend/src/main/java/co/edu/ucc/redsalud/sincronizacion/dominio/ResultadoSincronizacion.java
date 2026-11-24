package co.edu.ucc.redsalud.sincronizacion.dominio;

public record ResultadoSincronizacion(
        String idempotencyKey,
        String agregadoId,
        String estado,
        int codigoHttp,
        String mensaje,
        boolean duplicado,
        boolean conflictoAuditado) {

    public ResultadoSincronizacion comoReintentoIdempotente() {
        return new ResultadoSincronizacion(idempotencyKey, agregadoId, estado, 200, mensaje, true, conflictoAuditado);
    }
}
