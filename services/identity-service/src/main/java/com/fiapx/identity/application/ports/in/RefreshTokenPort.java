package com.fiapx.identity.application.ports.in;

import com.fiapx.identity.application.dto.AuthResult;

public interface RefreshTokenPort {

    AuthResult execute(String rawRefreshToken);
}
