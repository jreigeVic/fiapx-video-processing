package com.fiapx.identity.application.usecase;

import com.fiapx.identity.application.ports.out.UserRepositoryPort;
import com.fiapx.identity.domain.exception.InvalidCredentialsException;
import com.fiapx.identity.domain.model.User;
import java.util.UUID;

public class GetAuthenticatedUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public GetAuthenticatedUserUseCase(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    public User execute(UUID authenticatedUserId) {
        return userRepositoryPort
                .findById(authenticatedUserId)
                .orElseThrow(InvalidCredentialsException::new);
    }
}
