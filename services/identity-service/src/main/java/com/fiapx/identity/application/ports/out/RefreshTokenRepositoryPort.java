package com.fiapx.identity.application.ports.out;

import com.fiapx.identity.domain.model.RefreshToken;
import java.util.Optional;

public interface RefreshTokenRepositoryPort {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
