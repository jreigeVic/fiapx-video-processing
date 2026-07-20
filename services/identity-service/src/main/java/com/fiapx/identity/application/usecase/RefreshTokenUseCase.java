package com.fiapx.identity.application.usecase;

import com.fiapx.identity.application.dto.AccessToken;
import com.fiapx.identity.application.dto.AuthResult;
import com.fiapx.identity.application.ports.out.RefreshTokenRepositoryPort;
import com.fiapx.identity.application.ports.out.TokenProviderPort;
import com.fiapx.identity.application.ports.out.UserRepositoryPort;
import com.fiapx.identity.domain.exception.InvalidRefreshTokenException;
import com.fiapx.identity.domain.model.IssuedRefreshToken;
import com.fiapx.identity.domain.model.RefreshToken;
import com.fiapx.identity.domain.model.User;
import java.time.Duration;
import java.time.Instant;

public class RefreshTokenUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final TokenProviderPort tokenProviderPort;
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final Duration refreshTokenTimeToLive;

    public RefreshTokenUseCase(UserRepositoryPort userRepositoryPort,
                                TokenProviderPort tokenProviderPort,
                                RefreshTokenRepositoryPort refreshTokenRepositoryPort,
                                Duration refreshTokenTimeToLive) {
        this.userRepositoryPort = userRepositoryPort;
        this.tokenProviderPort = tokenProviderPort;
        this.refreshTokenRepositoryPort = refreshTokenRepositoryPort;
        this.refreshTokenTimeToLive = refreshTokenTimeToLive;
    }

    public AuthResult execute(String rawRefreshToken) {
        String tokenHash = RefreshToken.hash(rawRefreshToken);
        RefreshToken existing = refreshTokenRepositoryPort.findByTokenHash(tokenHash)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (!existing.isValid(Instant.now())) {
            throw new InvalidRefreshTokenException();
        }

        existing.revoke();
        refreshTokenRepositoryPort.save(existing);

        User user = userRepositoryPort.findById(existing.getUserId())
                .orElseThrow(InvalidRefreshTokenException::new);

        AccessToken accessToken = tokenProviderPort.generateAccessToken(user);
        IssuedRefreshToken issued = RefreshToken.issue(user.getId(), refreshTokenTimeToLive);
        refreshTokenRepositoryPort.save(issued.token());

        return new AuthResult(accessToken, issued.rawValue());
    }
}
