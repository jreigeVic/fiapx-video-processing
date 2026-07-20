package com.fiapx.identity.api.controller;

import com.fiapx.identity.api.mapper.AuthMapper;
import com.fiapx.identity.api.request.LoginRequest;
import com.fiapx.identity.api.request.RefreshTokenRequest;
import com.fiapx.identity.api.request.RegisterUserRequest;
import com.fiapx.identity.api.response.LoginResponse;
import com.fiapx.identity.api.response.UserResponse;
import com.fiapx.identity.application.dto.AuthResult;
import com.fiapx.identity.application.usecase.AuthenticateUserUseCase;
import com.fiapx.identity.application.usecase.GetAuthenticatedUserUseCase;
import com.fiapx.identity.application.usecase.LogoutUseCase;
import com.fiapx.identity.application.usecase.RefreshTokenUseCase;
import com.fiapx.identity.application.usecase.RegisterUserUseCase;
import com.fiapx.identity.domain.model.User;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final GetAuthenticatedUserUseCase getAuthenticatedUserUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase,
                           AuthenticateUserUseCase authenticateUserUseCase,
                           RefreshTokenUseCase refreshTokenUseCase,
                           LogoutUseCase logoutUseCase,
                           GetAuthenticatedUserUseCase getAuthenticatedUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
        this.getAuthenticatedUserUseCase = getAuthenticatedUserUseCase;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterUserRequest request) {
        User user = registerUserUseCase.execute(request.name(), request.email(), request.password());
        return AuthMapper.toUserResponse(user);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        AuthResult result = authenticateUserUseCase.execute(request.email(), request.password());
        return AuthMapper.toLoginResponse(result);
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        User user = getAuthenticatedUserUseCase.execute(currentUserId(authentication));
        return AuthMapper.toUserResponse(user);
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResult result = refreshTokenUseCase.execute(request.refreshToken());
        return AuthMapper.toLoginResponse(result);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(Authentication authentication, @Valid @RequestBody RefreshTokenRequest request) {
        logoutUseCase.execute(currentUserId(authentication), request.refreshToken());
    }

    private UUID currentUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }
}
