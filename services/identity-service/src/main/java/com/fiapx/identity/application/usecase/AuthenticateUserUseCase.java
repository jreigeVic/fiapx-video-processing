package com.fiapx.identity.application.usecase;

import com.fiapx.identity.application.dto.AccessToken;
import com.fiapx.identity.application.dto.AuthResult;
import com.fiapx.identity.application.ports.out.PasswordEncoderPort;
import com.fiapx.identity.application.ports.out.RefreshTokenRepositoryPort;
import com.fiapx.identity.application.ports.out.TokenProviderPort;
import com.fiapx.identity.application.ports.out.UserRepositoryPort;
import com.fiapx.identity.domain.exception.InvalidCredentialsException;
import com.fiapx.identity.domain.model.Email;
import com.fiapx.identity.domain.model.IssuedRefreshToken;
import com.fiapx.identity.domain.model.RefreshToken;
import com.fiapx.identity.domain.model.User;
import java.time.Duration;

public class AuthenticateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenProviderPort tokenProviderPort;
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final Duration refreshTokenTimeToLive;

    public AuthenticateUserUseCase(UserRepositoryPort userRepositoryPort,
                                    PasswordEncoderPort passwordEncoderPort,
                                    TokenProviderPort tokenProviderPort,
                                    RefreshTokenRepositoryPort refreshTokenRepositoryPort,
                                    Duration refreshTokenTimeToLive) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.tokenProviderPort = tokenProviderPort;
        this.refreshTokenRepositoryPort = refreshTokenRepositoryPort;
        this.refreshTokenTimeToLive = refreshTokenTimeToLive;
    }

    public AuthResult execute(String rawEmail, String rawPassword) {
        Email email = Email.of(rawEmail);
        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoderPort.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        AccessToken accessToken = tokenProviderPort.generateAccessToken(user);
        IssuedRefreshToken issued = RefreshToken.issue(user.getId(), refreshTokenTimeToLive);
        refreshTokenRepositoryPort.save(issued.token());

        return new AuthResult(accessToken, issued.rawValue());
    }
}
