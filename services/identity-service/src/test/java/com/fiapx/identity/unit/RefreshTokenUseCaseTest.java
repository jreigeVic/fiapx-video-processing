package com.fiapx.identity.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiapx.identity.application.dto.AccessToken;
import com.fiapx.identity.application.dto.AuthResult;
import com.fiapx.identity.application.ports.out.RefreshTokenRepositoryPort;
import com.fiapx.identity.application.ports.out.TokenProviderPort;
import com.fiapx.identity.application.ports.out.UserRepositoryPort;
import com.fiapx.identity.application.usecase.RefreshTokenUseCase;
import com.fiapx.identity.domain.exception.InvalidRefreshTokenException;
import com.fiapx.identity.domain.model.Email;
import com.fiapx.identity.domain.model.IssuedRefreshToken;
import com.fiapx.identity.domain.model.PasswordHash;
import com.fiapx.identity.domain.model.RefreshToken;
import com.fiapx.identity.domain.model.User;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RefreshTokenUseCaseTest {

    private final UserRepositoryPort userRepositoryPort = mock(UserRepositoryPort.class);
    private final TokenProviderPort tokenProviderPort = mock(TokenProviderPort.class);
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort =
            mock(RefreshTokenRepositoryPort.class);
    private final RefreshTokenUseCase useCase =
            new RefreshTokenUseCase(
                    userRepositoryPort,
                    tokenProviderPort,
                    refreshTokenRepositoryPort,
                    Duration.ofDays(7));

    @Test
    void rotatesValidRefreshToken() {
        UUID userId = UUID.randomUUID();
        IssuedRefreshToken issued = RefreshToken.issue(userId, Duration.ofMinutes(5));
        when(refreshTokenRepositoryPort.findByTokenHash(RefreshToken.hash(issued.rawValue())))
                .thenReturn(Optional.of(issued.token()));
        User user =
                User.register("Jane", Email.of("jane@user.com"), PasswordHash.fromHash("hashed"));
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));
        when(tokenProviderPort.generateAccessToken(user))
                .thenReturn(new AccessToken("new-jwt", 900));

        AuthResult result = useCase.execute(issued.rawValue());

        assertThat(result.accessToken().value()).isEqualTo("new-jwt");
        assertThat(issued.token().isRevoked()).isTrue();
        verify(refreshTokenRepositoryPort, times(2)).save(any());
    }

    @Test
    void rejectsUnknownToken() {
        when(refreshTokenRepositoryPort.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("unknown-raw-value"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void rejectsExpiredToken() {
        UUID userId = UUID.randomUUID();
        IssuedRefreshToken issued = RefreshToken.issue(userId, Duration.ofSeconds(-1));
        when(refreshTokenRepositoryPort.findByTokenHash(RefreshToken.hash(issued.rawValue())))
                .thenReturn(Optional.of(issued.token()));

        assertThatThrownBy(() -> useCase.execute(issued.rawValue()))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }
}
