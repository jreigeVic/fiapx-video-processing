package com.fiapx.identity.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiapx.identity.application.dto.AccessToken;
import com.fiapx.identity.application.dto.AuthResult;
import com.fiapx.identity.application.ports.out.PasswordEncoderPort;
import com.fiapx.identity.application.ports.out.RefreshTokenRepositoryPort;
import com.fiapx.identity.application.ports.out.TokenProviderPort;
import com.fiapx.identity.application.ports.out.UserRepositoryPort;
import com.fiapx.identity.application.usecase.AuthenticateUserUseCase;
import com.fiapx.identity.domain.exception.InvalidCredentialsException;
import com.fiapx.identity.domain.model.Email;
import com.fiapx.identity.domain.model.PasswordHash;
import com.fiapx.identity.domain.model.User;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AuthenticateUserUseCaseTest {

    private final UserRepositoryPort userRepositoryPort = mock(UserRepositoryPort.class);
    private final PasswordEncoderPort passwordEncoderPort = mock(PasswordEncoderPort.class);
    private final TokenProviderPort tokenProviderPort = mock(TokenProviderPort.class);
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort = mock(RefreshTokenRepositoryPort.class);
    private final AuthenticateUserUseCase useCase = new AuthenticateUserUseCase(
            userRepositoryPort, passwordEncoderPort, tokenProviderPort, refreshTokenRepositoryPort, Duration.ofDays(7));

    @Test
    void authenticatesValidCredentialsAndIssuesTokenPair() {
        User user = User.register("Jane", Email.of("jane@user.com"), PasswordHash.fromHash("hashed"));
        when(userRepositoryPort.findByEmail(Email.of("jane@user.com"))).thenReturn(Optional.of(user));
        when(passwordEncoderPort.matches("raw-password", user.getPasswordHash())).thenReturn(true);
        when(tokenProviderPort.generateAccessToken(user)).thenReturn(new AccessToken("jwt-value", 900));

        AuthResult result = useCase.execute("jane@user.com", "raw-password");

        assertThat(result.accessToken().value()).isEqualTo("jwt-value");
        assertThat(result.refreshToken()).isNotBlank();
        verify(refreshTokenRepositoryPort).save(any());
    }

    @Test
    void rejectsUnknownEmail() {
        when(userRepositoryPort.findByEmail(Email.of("missing@user.com"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("missing@user.com", "raw-password"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void rejectsWrongPassword() {
        User user = User.register("Jane", Email.of("jane@user.com"), PasswordHash.fromHash("hashed"));
        when(userRepositoryPort.findByEmail(Email.of("jane@user.com"))).thenReturn(Optional.of(user));
        when(passwordEncoderPort.matches("wrong", user.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute("jane@user.com", "wrong"))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
