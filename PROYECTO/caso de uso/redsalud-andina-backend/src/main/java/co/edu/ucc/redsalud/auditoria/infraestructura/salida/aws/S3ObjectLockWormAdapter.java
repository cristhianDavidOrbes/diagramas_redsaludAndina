package co.edu.ucc.redsalud.auditoria.infraestructura.salida.aws;

import co.edu.ucc.redsalud.auditoria.aplicacion.puertos.salida.ArchivoWormPort;
import co.edu.ucc.redsalud.auditoria.dominio.TrazaAccesoClinico;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectLockMode;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3ObjectLockWormAdapter implements ArchivoWormPort {

    private final S3Client s3Client;
    private final String bucket;

    public S3ObjectLockWormAdapter(S3Client s3Client, String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    @Override
    public void archivarCompliance(TrazaAccesoClinico t) {
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key("auditoria/" + t.recursoId() + "/" + t.trazaId() + ".log")
                .objectLockMode(ObjectLockMode.COMPLIANCE)
                .objectLockRetainUntilDate(Instant.now().plus(365L * 5, ChronoUnit.DAYS))
                .build();
        String linea = t.ocurridoEn() + "|" + t.usuarioId() + "|" + t.recursoId() + "|" + t.motivo() + "|" + t.hashActual();
        s3Client.putObject(req, RequestBody.fromString(linea));
    }
}
