package com.fiapx.identity.infrastructure.adapter.out;

import com.fiapx.identity.application.ports.out.PasswordEncoderPort;
import com.fiapx.identity.domain.model.PasswordHash;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptPasswordEncoderAdapter implements PasswordEncoderPort {

    private final BCryptPasswordEncoder delegate = new BCryptPasswordEncoder();

    @Override
    public PasswordHash encode(String rawPassword) {
        return PasswordHash.fromHash(delegate.encode(rawPassword));
    }

    @Override
    public boolean matches(String rawPassword, PasswordHash passwordHash) {
        return delegate.matches(rawPassword, passwordHash.value());
    }
}
