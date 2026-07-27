package com.fiapx.identity.application.usecase;

import com.fiapx.identity.application.ports.in.LogoutPort;
import com.fiapx.identity.application.ports.out.RefreshTokenRepositoryPort;
import com.fiapx.identity.domain.exception.InvalidRefreshTokenException;
import com.fiapx.identity.domain.model.RefreshToken;
import java.util.UUID;

public class LogoutUseCase implements LogoutPort {

    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;

    public LogoutUseCase(RefreshTokenRepositoryPort refreshTokenRepositoryPort) {
        this.refreshTokenRepositoryPort = refreshTokenRepositoryPort;
    }

    @Override
    public void execute(UUID authenticatedUserId, String rawRefreshToken) {
        String tokenHash = RefreshToken.hash(rawRefreshToken);
        RefreshToken existing =
                refreshTokenRepositoryPort
                        .findByTokenHash(tokenHash)
                        .orElseThrow(InvalidRefreshTokenException::new);

        if (!existing.getUserId().equals(authenticatedUserId)) {
            throw new InvalidRefreshTokenException();
        }

        existing.revoke();
        refreshTokenRepositoryPort.save(existing);
    }
}
