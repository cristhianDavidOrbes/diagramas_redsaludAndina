package co.edu.ucc.redsalud.episodio.infraestructura.salida.aws;

import co.edu.ucc.redsalud.episodio.aplicacion.puertos.salida.AdjuntoStoragePort;
import co.edu.ucc.redsalud.episodio.dominio.AdjuntoCifrado;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;

public class S3AdjuntoKmsAdapter implements AdjuntoStoragePort {

    private final S3Client s3Client;
    private final String bucket;
    private final String kmsKeyId;
    private final String regionAws;

    public S3AdjuntoKmsAdapter(S3Client s3Client, String bucket, String kmsKeyId, String regionAws) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.kmsKeyId = kmsKeyId;
        this.regionAws = regionAws;
    }

    @Override
    public AdjuntoCifrado almacenarCifrado(String episodioId, String nombreArchivo, byte[] contenido) {
        String key = "episodios/" + episodioId + "/" + nombreArchivo;
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .serverSideEncryption(ServerSideEncryption.AWS_KMS)
                .ssekmsKeyId(kmsKeyId)
                .build();
        s3Client.putObject(req, RequestBody.fromBytes(contenido));
        return new AdjuntoCifrado(key, kmsKeyId, regionAws, false);
    }

    public String politicaBucketRegionUnicaJson() {
        return """
                {"Version":"2012-10-17","Statement":[
                  {"Effect":"Deny","Action":"s3:PutBucketReplication","Resource":"arn:aws:s3:::%s"},
                  {"Effect":"Deny","Action":"s3:PutObject","Resource":"arn:aws:s3:::%s/*",
                   "Condition":{"StringNotEquals":{"s3:x-amz-server-side-encryption":"aws:kms"}}}
                ]}""".formatted(bucket, bucket);
    }
}
