package com.fiapx.identity.application.usecase;

import com.fiapx.identity.application.ports.out.PasswordEncoderPort;
import com.fiapx.identity.application.ports.out.UserRepositoryPort;
import com.fiapx.identity.domain.exception.EmailAlreadyRegisteredException;
import com.fiapx.identity.domain.model.Email;
import com.fiapx.identity.domain.model.PasswordHash;
import com.fiapx.identity.domain.model.User;

public class RegisterUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;

    public RegisterUserUseCase(UserRepositoryPort userRepositoryPort, PasswordEncoderPort passwordEncoderPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    public User execute(String name, String rawEmail, String rawPassword) {
        Email email = Email.of(rawEmail);
        if (userRepositoryPort.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(email.value());
        }
        PasswordHash passwordHash = passwordEncoderPort.encode(rawPassword);
        User user = User.register(name, email, passwordHash);
        return userRepositoryPort.save(user);
    }
}
