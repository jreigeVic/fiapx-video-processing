package com.fiapx.identity.infrastructure.adapter.out;

import com.fiapx.identity.application.ports.out.RefreshTokenRepositoryPort;
import com.fiapx.identity.domain.model.RefreshToken;
import com.fiapx.identity.infrastructure.repository.RefreshTokenJpaEntity;
import com.fiapx.identity.infrastructure.repository.RefreshTokenJpaRepository;
import java.util.Optional;

public class JpaRefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

    private final RefreshTokenJpaRepository jpaRepository;

    public JpaRefreshTokenRepositoryAdapter(RefreshTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity(
                refreshToken.getId(),
                refreshToken.getUserId(),
                refreshToken.getTokenHash(),
                refreshToken.getExpiresAt(),
                refreshToken.isRevoked(),
                refreshToken.getCreatedAt());
        jpaRepository.save(entity);
        return refreshToken;
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    private RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return RefreshToken.reconstruct(
                entity.getId(),
                entity.getUserId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.isRevoked(),
                entity.getCreatedAt());
    }
}
