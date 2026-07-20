package com.fiapx.identity.api.mapper;

import com.fiapx.identity.api.response.LoginResponse;
import com.fiapx.identity.api.response.UserResponse;
import com.fiapx.identity.application.dto.AuthResult;
import com.fiapx.identity.domain.model.User;

public final class AuthMapper {

    private AuthMapper() {
    }

    public static UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail().value());
    }

    public static LoginResponse toLoginResponse(AuthResult authResult) {
        return new LoginResponse(
                authResult.accessToken().value(),
                "Bearer",
                authResult.accessToken().expiresInSeconds(),
                authResult.refreshToken());
    }
}
