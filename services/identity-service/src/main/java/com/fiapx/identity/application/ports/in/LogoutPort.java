package com.fiapx.identity.application.ports.in;

import java.util.UUID;

public interface LogoutPort {

    void execute(UUID authenticatedUserId, String rawRefreshToken);
}
