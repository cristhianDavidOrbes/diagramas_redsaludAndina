package co.edu.ucc.redsalud.identidad.infraestructura.salida.oidc;

import co.edu.ucc.redsalud.identidad.aplicacion.puertos.salida.EmisorTokenPort;
import co.edu.ucc.redsalud.identidad.dominio.IdentidadException;
import co.edu.ucc.redsalud.identidad.dominio.PrincipalClinico;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

public class JwtLocalDemoAdapter implements EmisorTokenPort {

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;

    public JwtLocalDemoAdapter() {
        KeyPair kp = generarRsa();
        RSAKey jwk = new RSAKey.Builder((RSAPublicKey) kp.getPublic())
                .privateKey((RSAPrivateKey) kp.getPrivate())
                .keyID("redsalud-demo-rs256")
                .build();
        this.encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
        this.decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) kp.getPublic()).build();
    }

    @Override
    public String firmarJwt(PrincipalClinico p) {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("https://auth.redsaludandina.local/realms/teleconsulta")
                .subject(p.usuarioId())
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(1800))
                .claim("rol", p.rol().name())
                .claim("centro_id", p.centroId())
                .claim("especialidad", p.especialidad())
                .claim("pacientes", List.copyOf(p.pacientesAsignados()))
                .build();
        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public Optional<Jwt> verificar(String token) {
        try {
            return Optional.of(decoder.decode(token));
        } catch (JwtException ex) {
            return Optional.empty();
        }
    }

    private static KeyPair generarRsa() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            return gen.generateKeyPair();
        } catch (NoSuchAlgorithmException ex) {
            throw new IdentidadException("RSA no disponible para emitir JWT demo");
        }
    }
}
