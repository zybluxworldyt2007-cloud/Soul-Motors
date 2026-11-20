package com.soulmotors.api.auth;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.soulmotors.api.usuario.Usuario;

@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final String issuer;
    private final Duration expiracion;

    public JwtService(JwtEncoder encoder,
                      @Value("${app.jwt.issuer}") String issuer,
                      @Value("${app.jwt.expiration-minutes}") long expiracionMinutos) {
        this.encoder = encoder;
        this.issuer = issuer;
        this.expiracion = Duration.ofMinutes(expiracionMinutos);
    }

    public TokenEmitido emitir(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(expiracion);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(usuario.getUsername())
                .issuedAt(ahora)
                .expiresAt(expira)
                .claim("uid", usuario.getId())
                .claim("nombre", usuario.getNombre())
                .claim("rol", usuario.getRol().name())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenEmitido(token, expira);
    }

    public record TokenEmitido(String token, Instant expiraEn) {
    }
}
