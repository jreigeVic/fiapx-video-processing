package com.fiapx.identity.infrastructure.adapter.out;

import com.fiapx.identity.application.ports.out.UserRepositoryPort;
import com.fiapx.identity.domain.model.Email;
import com.fiapx.identity.domain.model.PasswordHash;
import com.fiapx.identity.domain.model.User;
import com.fiapx.identity.infrastructure.repository.UserJpaEntity;
import com.fiapx.identity.infrastructure.repository.UserJpaRepository;
import java.util.Optional;
import java.util.UUID;

public class JpaUserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository jpaRepository;

    public JpaUserRepositoryAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = new UserJpaEntity(
                user.getId(),
                user.getName(),
                user.getEmail().value(),
                user.getPasswordHash().value(),
                user.getCreatedAt());
        jpaRepository.save(entity);
        return user;
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return jpaRepository.findByEmail(email.value()).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpaRepository.existsByEmail(email.value());
    }

    private User toDomain(UserJpaEntity entity) {
        return User.reconstruct(
                entity.getId(),
                entity.getName(),
                Email.of(entity.getEmail()),
                PasswordHash.fromHash(entity.getPasswordHash()),
                entity.getCreatedAt());
    }
}
