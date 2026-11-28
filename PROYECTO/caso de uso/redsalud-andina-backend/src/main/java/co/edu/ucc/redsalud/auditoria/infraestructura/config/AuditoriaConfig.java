package co.edu.ucc.redsalud.auditoria.infraestructura.config;

import co.edu.ucc.redsalud.auditoria.aplicacion.puertos.entrada.AuditarAccesoUseCase;
import co.edu.ucc.redsalud.auditoria.aplicacion.puertos.salida.ArchivoWormPort;
import co.edu.ucc.redsalud.auditoria.aplicacion.puertos.salida.TrazaAuditoriaRepository;
import co.edu.ucc.redsalud.auditoria.aplicacion.servicios.AuditarAccesoService;
import co.edu.ucc.redsalud.auditoria.dominio.ReporteAuditoria;
import co.edu.ucc.redsalud.auditoria.dominio.TrazaAccesoClinico;
import co.edu.ucc.redsalud.auditoria.infraestructura.salida.aws.S3ObjectLockWormAdapter;
import co.edu.ucc.redsalud.auditoria.infraestructura.salida.jpa.AuditoriaJpaAdapter;
import co.edu.ucc.redsalud.auditoria.infraestructura.salida.jpa.TrazaAuditoriaJpaRepository;
import co.edu.ucc.redsalud.auditoria.infraestructura.salida.memoria.AuditoriaMemoriaAdapter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AuditoriaConfig {

    @Bean
    @Profile("esqueleto")
    public AuditoriaMemoriaAdapter auditoriaMemoriaAdapter() { return new AuditoriaMemoriaAdapter(); }

    @Bean
    @Profile("postgres")
    public TrazaAuditoriaRepository auditoriaJpaRepositoryAdapter(TrazaAuditoriaJpaRepository jpa) {
        return new AuditoriaJpaAdapter(jpa);
    }

    @Bean
    @Profile("postgres")
    public ArchivoWormPort s3ObjectLockWormAdapter(
            ObjectProvider<S3Client> s3Provider, @Value("${redsalud.aws.s3-auditoria-bucket}") String bucket) {
        S3Client s3 = s3Provider.getIfAvailable();
        return s3 != null ? new S3ObjectLockWormAdapter(s3, bucket) : new AuditoriaMemoriaAdapter();
    }

    @Bean
    public AuditarAccesoUseCase auditarAccesoUseCase(
            TrazaAuditoriaRepository repo, ArchivoWormPort worm, ObjectProvider<PlatformTransactionManager> tmProvider) {
        AuditarAccesoService servicio = new AuditarAccesoService(repo, worm);
        PlatformTransactionManager tm = tmProvider.getIfAvailable();
        if (tm == null) { return servicio; }
        TransactionTemplate txRequiresNew = new TransactionTemplate(tm);
        txRequiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return new AuditarAccesoUseCase() {
            @Override
            public TrazaAccesoClinico registrar(String u, String r, String rec, String m, boolean p, String c) {
                return txRequiresNew.execute(s -> servicio.registrar(u, r, rec, m, p, c));
            }
            @Override
            public ReporteAuditoria generarReporte(String recursoId) { return servicio.generarReporte(recursoId); }
            @Override
            public boolean verificarIntegridadCadena() { return servicio.verificarIntegridadCadena(); }
        };
    }
}
