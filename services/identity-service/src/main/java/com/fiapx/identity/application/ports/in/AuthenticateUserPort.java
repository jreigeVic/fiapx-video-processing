package com.fiapx.identity.application.ports.in;

import com.fiapx.identity.application.dto.AuthResult;

public interface AuthenticateUserPort {

    AuthResult execute(String rawEmail, String rawPassword);
}
