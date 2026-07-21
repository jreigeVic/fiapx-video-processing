package com.fiapx.identity.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiapx.identity.application.ports.out.RefreshTokenRepositoryPort;
import com.fiapx.identity.application.usecase.LogoutUseCase;
import com.fiapx.identity.domain.exception.InvalidRefreshTokenException;
import com.fiapx.identity.domain.model.IssuedRefreshToken;
import com.fiapx.identity.domain.model.RefreshToken;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LogoutUseCaseTest {

    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort =
            mock(RefreshTokenRepositoryPort.class);
    private final LogoutUseCase useCase = new LogoutUseCase(refreshTokenRepositoryPort);

    @Test
    void revokesOwnedToken() {
        UUID userId = UUID.randomUUID();
        IssuedRefreshToken issued = RefreshToken.issue(userId, Duration.ofMinutes(5));
        when(refreshTokenRepositoryPort.findByTokenHash(RefreshToken.hash(issued.rawValue())))
                .thenReturn(Optional.of(issued.token()));

        useCase.execute(userId, issued.rawValue());

        assertThat(issued.token().isRevoked()).isTrue();
        verify(refreshTokenRepositoryPort).save(issued.token());
    }

    @Test
    void rejectsTokenOwnedByAnotherUser() {
        UUID owner = UUID.randomUUID();
        IssuedRefreshToken issued = RefreshToken.issue(owner, Duration.ofMinutes(5));
        when(refreshTokenRepositoryPort.findByTokenHash(RefreshToken.hash(issued.rawValue())))
                .thenReturn(Optional.of(issued.token()));

        assertThatThrownBy(() -> useCase.execute(UUID.randomUUID(), issued.rawValue()))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }
}
