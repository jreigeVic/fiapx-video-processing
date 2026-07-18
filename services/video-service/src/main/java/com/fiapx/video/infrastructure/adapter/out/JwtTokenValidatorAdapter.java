package com.fiapx.video.infrastructure.adapter.out;

import com.fiapx.video.application.dto.AuthenticatedUser;
import com.fiapx.video.application.ports.out.TokenValidatorPort;
import com.fiapx.video.domain.exception.InvalidAccessTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.UUID;

public class JwtTokenValidatorAdapter implements TokenValidatorPort {

    private final Key signingKey;

    public JwtTokenValidatorAdapter(String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public AuthenticatedUser validate(String accessToken) {
        try {
            Claims claims =
                    Jwts.parserBuilder()
                            .setSigningKey(signingKey)
                            .build()
                            .parseClaimsJws(accessToken)
                            .getBody();
            UUID userId = UUID.fromString(claims.getSubject());
            String email = claims.get("email", String.class);
            return new AuthenticatedUser(userId, email);
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidAccessTokenException(e);
        }
    }
}
