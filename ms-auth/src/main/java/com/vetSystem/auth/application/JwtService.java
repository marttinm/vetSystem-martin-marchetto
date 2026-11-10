package com.vetSystem.auth.application;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// Crea y valida los tokens JWT (Header.Payload.Signature).
// La firma es HMAC-SHA256 con la clave secreta: si alguien modifica el payload la firma
// deja de coincidir y el token se rechaza, sin consultar la base de datos.
@Service
public class JwtService {

    // 86.400.000 ms = 24 horas
    private static final long EXPIRACION_MS = 86_400_000L;

    @Value("${jwt.secret}")
    private String secretKey;

    public String generateToken(String username, String rol) {
        return Jwts.builder()
                .subject(username)                  // claim "sub"
                .claim("rol", rol)
                .issuedAt(new Date())               // claim "iat"
                .expiration(new Date(System.currentTimeMillis() + EXPIRACION_MS)) // claim "exp"
                .signWith(getSigningKey())          // HS256
                .compact();
    }

    // Si la firma no coincide, el token vencio o esta mal formado, el parser lanza una excepcion
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // JJWT 0.12 exige una clave de al menos 256 bits (32 caracteres) para HS256
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}
