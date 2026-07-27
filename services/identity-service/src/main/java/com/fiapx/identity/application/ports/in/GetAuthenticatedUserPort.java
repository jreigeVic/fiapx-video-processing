package com.fiapx.identity.application.ports.in;

import com.fiapx.identity.domain.model.User;
import java.util.UUID;

public interface GetAuthenticatedUserPort {

    User execute(UUID authenticatedUserId);
}
