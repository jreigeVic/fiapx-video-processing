package com.fiapx.identity.infrastructure.adapter.out;

import com.fiapx.identity.application.dto.AccessToken;
import com.fiapx.identity.application.ports.out.TokenProviderPort;
import com.fiapx.identity.domain.exception.InvalidAccessTokenException;
import com.fiapx.identity.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class JwtTokenProviderAdapter implements TokenProviderPort {

    private final Key signingKey;
    private final long accessTokenExpirationSeconds;

    public JwtTokenProviderAdapter(String secret, long accessTokenExpirationSeconds) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
    }

    @Override
    public AccessToken generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessTokenExpirationSeconds);
        String token = Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getEmail().value())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
        return new AccessToken(token, accessTokenExpirationSeconds);
    }

    @Override
    public UUID validateAndGetUserId(String accessToken) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
            return UUID.fromString(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidAccessTokenException();
        }
    }
}
