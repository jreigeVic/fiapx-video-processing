package com.fiapx.identity.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiapx.identity.application.ports.out.PasswordEncoderPort;
import com.fiapx.identity.application.ports.out.UserRepositoryPort;
import com.fiapx.identity.application.usecase.RegisterUserUseCase;
import com.fiapx.identity.domain.exception.EmailAlreadyRegisteredException;
import com.fiapx.identity.domain.model.Email;
import com.fiapx.identity.domain.model.PasswordHash;
import com.fiapx.identity.domain.model.User;
import org.junit.jupiter.api.Test;

class RegisterUserUseCaseTest {

    private final UserRepositoryPort userRepositoryPort = mock(UserRepositoryPort.class);
    private final PasswordEncoderPort passwordEncoderPort = mock(PasswordEncoderPort.class);
    private final RegisterUserUseCase useCase = new RegisterUserUseCase(userRepositoryPort, passwordEncoderPort);

    @Test
    void registersNewUserWithEncodedPassword() {
        when(userRepositoryPort.existsByEmail(Email.of("new@user.com"))).thenReturn(false);
        when(passwordEncoderPort.encode("raw-password")).thenReturn(PasswordHash.fromHash("hashed"));
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = useCase.execute("New User", "new@user.com", "raw-password");

        assertThat(user.getEmail()).isEqualTo(Email.of("new@user.com"));
        assertThat(user.getPasswordHash().value()).isEqualTo("hashed");
        verify(userRepositoryPort).save(user);
    }

    @Test
    void rejectsAlreadyRegisteredEmail() {
        when(userRepositoryPort.existsByEmail(Email.of("dup@user.com"))).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute("Dup", "dup@user.com", "raw-password"))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
    }
}
