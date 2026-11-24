package co.edu.ucc.redsalud.receta.infraestructura.salida.aws;

import co.edu.ucc.redsalud.receta.aplicacion.puertos.salida.DespachoFarmaciaPort;
import co.edu.ucc.redsalud.receta.aplicacion.puertos.salida.FirmadorKmsPort;
import co.edu.ucc.redsalud.receta.dominio.FirmaKms;
import co.edu.ucc.redsalud.receta.dominio.RecetaException;
import co.edu.ucc.redsalud.receta.dominio.RecetaMedica;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class SqsFarmaciaAdapter implements DespachoFarmaciaPort, FirmadorKmsPort {

    private final SqsAsyncClient sqs;
    private final String queueUrl;
    private final String kmsKeyId;

    public SqsFarmaciaAdapter(SqsAsyncClient sqs, String queueUrl, String kmsKeyId) {
        this.sqs = sqs;
        this.queueUrl = queueUrl;
        this.kmsKeyId = kmsKeyId;
    }

    @Override
    public FirmaKms firmar(String contenido) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(contenido.getBytes(StandardCharsets.UTF_8));
            return new FirmaKms(HexFormat.of().formatHex(digest), kmsKeyId, Instant.now());
        } catch (NoSuchAlgorithmException ex) {
            throw new RecetaException("Algoritmo SHA-256 no disponible");
        }
    }

    @Override
    public void publicarEnColaFarmacia(RecetaMedica r) {
        SendMessageRequest req = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageGroupId(r.recetaId())
                .messageDeduplicationId(r.firma().hashSha256())
                .messageBody(r.recetaId() + "|" + r.pacienteId() + "|" + r.medicamentos())
                .build();
        sqs.sendMessage(req).join();
    }
}
