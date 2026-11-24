package co.edu.ucc.redsalud.sincronizacion.infraestructura.salida.aws;

import co.edu.ucc.redsalud.sincronizacion.aplicacion.puertos.salida.PublicadorOutboxPort;
import co.edu.ucc.redsalud.sincronizacion.dominio.MensajeOutbox;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class SqsFifoOutboxAdapter implements PublicadorOutboxPort {

    private final SqsAsyncClient sqs;
    private final String queueUrl;

    public SqsFifoOutboxAdapter(SqsAsyncClient sqs, String queueUrl) {
        this.sqs = sqs;
        this.queueUrl = queueUrl;
    }

    @Override
    public void publicarOrdenado(MensajeOutbox evento) {
        SendMessageRequest req = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageGroupId(evento.agregadoId())
                .messageDeduplicationId(evento.eventoId())
                .messageBody(evento.tipoEvento() + "|" + evento.payload())
                .build();
        sqs.sendMessage(req).join();
    }
}
