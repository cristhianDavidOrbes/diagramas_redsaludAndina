package co.edu.ucc.redsalud.identidad.infraestructura.config;

import co.edu.ucc.redsalud.identidad.aplicacion.puertos.entrada.EmitirTokenDemoUseCase;
import co.edu.ucc.redsalud.identidad.aplicacion.puertos.entrada.EvaluarAccesoAbacUseCase;
import co.edu.ucc.redsalud.identidad.aplicacion.puertos.salida.EmisorTokenPort;
import co.edu.ucc.redsalud.identidad.aplicacion.puertos.salida.IdentidadRepository;
import co.edu.ucc.redsalud.identidad.aplicacion.puertos.salida.RegistroDenegacionPort;
import co.edu.ucc.redsalud.identidad.aplicacion.servicios.EmitirTokenDemoService;
import co.edu.ucc.redsalud.identidad.aplicacion.servicios.EvaluarAccesoAbacService;
import co.edu.ucc.redsalud.identidad.dominio.DecisionAcceso;
import co.edu.ucc.redsalud.identidad.infraestructura.salida.jpa.IdentidadJpaAdapter;
import co.edu.ucc.redsalud.identidad.infraestructura.salida.jpa.IdentidadJpaRepository;
import co.edu.ucc.redsalud.identidad.infraestructura.salida.memoria.IdentidadMemoriaAdapter;
import co.edu.ucc.redsalud.identidad.infraestructura.salida.oidc.JwtLocalDemoAdapter;
import co.edu.ucc.redsalud.identidad.infraestructura.salida.oidc.TraductorJwtOidc;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@Configuration
@EnableMethodSecurity
public class IdentidadConfig {

    @Bean
    @Profile("esqueleto")
    public IdentidadMemoriaAdapter identidadMemoriaAdapter() { return new IdentidadMemoriaAdapter(); }

    @Bean
    @Profile("postgres")
    public IdentidadJpaAdapter identidadJpaAdapter(IdentidadJpaRepository jpa) { return new IdentidadJpaAdapter(jpa); }

    @Bean
    public TraductorJwtOidc traductorJwtOidc() { return new TraductorJwtOidc(); }

    @Bean
    public JwtLocalDemoAdapter jwtLocalDemoAdapter() { return new JwtLocalDemoAdapter(); }

    @Bean
    public EmitirTokenDemoUseCase emitirTokenDemoUseCase(IdentidadRepository repo, EmisorTokenPort emisor) {
        return new EmitirTokenDemoService(repo, emisor);
    }

    @Bean
    public EvaluarAccesoAbacUseCase evaluarAccesoAbacUseCase(IdentidadRepository repo, RegistroDenegacionPort reg) {
        return new EvaluarAccesoAbacService(repo, reg);
    }

    @Bean("abacGuard")
    public AbacGuard abacGuard(EvaluarAccesoAbacUseCase useCase) { return new AbacGuard(useCase); }

    @Bean
    public PoliticaAbacGate politicaAbacGate(EvaluarAccesoAbacUseCase useCase) { return new PoliticaAbacGate(useCase); }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, EvaluarAccesoAbacUseCase useCase,
            JwtLocalDemoAdapter jwtAdapter, TraductorJwtOidc traductor) throws Exception {
        AuthorizationManager<RequestAuthorizationContext> abacManager = (auth, ctx) -> {
            HttpServletRequest req = ctx.getRequest();
            String usuarioId = resolverUsuario(req, jwtAdapter, traductor);
            if (usuarioId == null) {
                return new AuthorizationDecision(false);
            }
            DecisionAcceso d = useCase.evaluar(usuarioId, req.getHeader("X-Paciente-Id"),
                    req.getHeader("X-Centro-Id"), req.getRequestURI(), req.getHeader("X-Motivo"));
            return new AuthorizationDecision(d.permitido());
        };
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/v1/identidad/protegido/**").access(abacManager)
                        .anyRequest().permitAll())
                .build();
    }

    private String resolverUsuario(HttpServletRequest req, JwtLocalDemoAdapter jwtAdapter, TraductorJwtOidc traductor) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return jwtAdapter.verificar(authHeader.substring(7))
                    .map(jwt -> traductor.desdeJwt(jwt).usuarioId())
                    .orElse(null);
        }
        return req.getHeader("X-Usuario-Id");
    }

    public static class AbacGuard {
        private final EvaluarAccesoAbacUseCase useCase;
        public AbacGuard(EvaluarAccesoAbacUseCase useCase) { this.useCase = useCase; }
        public boolean permitir(String usuarioId, String pacienteId, String centro, String recurso, String motivo) {
            return useCase.evaluar(usuarioId, pacienteId, centro, recurso, motivo).permitido();
        }
    }
}
