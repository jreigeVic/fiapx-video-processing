package com.fiapx.identity.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiapx.identity.application.ports.out.UserRepositoryPort;
import com.fiapx.identity.domain.model.Email;
import com.fiapx.identity.domain.model.PasswordHash;
import com.fiapx.identity.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Runs the real Flyway migration (V001) against Postgres 16 to validate the
 * hexagonal persistence adapter end to end, per docs/LLD/identity-service.md's
 * testing strategy. Requires a running Docker daemon.
 */
@SpringBootTest
@Testcontainers
class JpaUserRepositoryAdapterIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("auth_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepositoryPort userRepositoryPort;

    @Test
    void persistsAndRetrievesUserByEmail() {
        Email email = Email.of("integration@user.com");
        User user = User.register("Integration User", email, PasswordHash.fromHash("hashed"));

        userRepositoryPort.save(user);

        assertThat(userRepositoryPort.existsByEmail(email)).isTrue();
        assertThat(userRepositoryPort.findByEmail(email)).isPresent();
        assertThat(userRepositoryPort.findById(user.getId())).isPresent();
    }
}
