package com.fiapx.identity.application.ports.out;

import com.fiapx.identity.domain.model.PasswordHash;

public interface PasswordEncoderPort {

    PasswordHash encode(String rawPassword);

    boolean matches(String rawPassword, PasswordHash passwordHash);
}
