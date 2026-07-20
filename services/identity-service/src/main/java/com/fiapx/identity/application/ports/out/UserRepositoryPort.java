package com.fiapx.identity.application.ports.out;

import com.fiapx.identity.domain.model.Email;
import com.fiapx.identity.domain.model.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findByEmail(Email email);

    Optional<User> findById(UUID id);

    boolean existsByEmail(Email email);
}
