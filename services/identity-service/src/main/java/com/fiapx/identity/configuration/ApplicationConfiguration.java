package com.fiapx.identity.configuration;

import com.fiapx.identity.application.ports.out.PasswordEncoderPort;
import com.fiapx.identity.application.ports.out.RefreshTokenRepositoryPort;
import com.fiapx.identity.application.ports.out.TokenProviderPort;
import com.fiapx.identity.application.ports.out.UserRepositoryPort;
import com.fiapx.identity.application.usecase.AuthenticateUserUseCase;
import com.fiapx.identity.application.usecase.GetAuthenticatedUserUseCase;
import com.fiapx.identity.application.usecase.LogoutUseCase;
import com.fiapx.identity.application.usecase.RefreshTokenUseCase;
import com.fiapx.identity.application.usecase.RegisterUserUseCase;
import com.fiapx.identity.infrastructure.adapter.in.JwtAuthenticationFilter;
import com.fiapx.identity.infrastructure.adapter.in.RestAuthenticationEntryPoint;
import com.fiapx.identity.infrastructure.adapter.out.BCryptPasswordEncoderAdapter;
import com.fiapx.identity.infrastructure.adapter.out.JpaRefreshTokenRepositoryAdapter;
import com.fiapx.identity.infrastructure.adapter.out.JpaUserRepositoryAdapter;
import com.fiapx.identity.infrastructure.adapter.out.JwtTokenProviderAdapter;
import com.fiapx.identity.infrastructure.repository.RefreshTokenJpaRepository;
import com.fiapx.identity.infrastructure.repository.UserJpaRepository;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class ApplicationConfiguration {

    @Bean
    public PasswordEncoderPort passwordEncoderPort() {
        return new BCryptPasswordEncoderAdapter();
    }

    @Bean
    public TokenProviderPort tokenProviderPort(JwtProperties jwtProperties) {
        return new JwtTokenProviderAdapter(
                jwtProperties.getSecret(), jwtProperties.getAccessTokenExpirationSeconds());
    }

    @Bean
    public UserRepositoryPort userRepositoryPort(UserJpaRepository userJpaRepository) {
        return new JpaUserRepositoryAdapter(userJpaRepository);
    }

    @Bean
    public RefreshTokenRepositoryPort refreshTokenRepositoryPort(
            RefreshTokenJpaRepository refreshTokenJpaRepository) {
        return new JpaRefreshTokenRepositoryAdapter(refreshTokenJpaRepository);
    }

    @Bean
    public RegisterUserUseCase registerUserUseCase(
            UserRepositoryPort userRepositoryPort, PasswordEncoderPort passwordEncoderPort) {
        return new RegisterUserUseCase(userRepositoryPort, passwordEncoderPort);
    }

    @Bean
    public AuthenticateUserUseCase authenticateUserUseCase(
            UserRepositoryPort userRepositoryPort,
            PasswordEncoderPort passwordEncoderPort,
            TokenProviderPort tokenProviderPort,
            RefreshTokenRepositoryPort refreshTokenRepositoryPort,
            JwtProperties jwtProperties) {
        return new AuthenticateUserUseCase(
                userRepositoryPort,
                passwordEncoderPort,
                tokenProviderPort,
                refreshTokenRepositoryPort,
                Duration.ofSeconds(jwtProperties.getRefreshTokenExpirationSeconds()));
    }

    @Bean
    public RefreshTokenUseCase refreshTokenUseCase(
            UserRepositoryPort userRepositoryPort,
            TokenProviderPort tokenProviderPort,
            RefreshTokenRepositoryPort refreshTokenRepositoryPort,
            JwtProperties jwtProperties) {
        return new RefreshTokenUseCase(
                userRepositoryPort,
                tokenProviderPort,
                refreshTokenRepositoryPort,
                Duration.ofSeconds(jwtProperties.getRefreshTokenExpirationSeconds()));
    }

    @Bean
    public LogoutUseCase logoutUseCase(RefreshTokenRepositoryPort refreshTokenRepositoryPort) {
        return new LogoutUseCase(refreshTokenRepositoryPort);
    }

    @Bean
    public GetAuthenticatedUserUseCase getAuthenticatedUserUseCase(
            UserRepositoryPort userRepositoryPort) {
        return new GetAuthenticatedUserUseCase(userRepositoryPort);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(TokenProviderPort tokenProviderPort) {
        return new JwtAuthenticationFilter(tokenProviderPort);
    }

    @Bean
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }
}
