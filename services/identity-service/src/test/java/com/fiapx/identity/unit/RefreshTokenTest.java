package com.fiapx.identity.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.fiapx.identity.domain.model.IssuedRefreshToken;
import com.fiapx.identity.domain.model.RefreshToken;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RefreshTokenTest {

    @Test
    void issuedTokenIsValidBeforeExpiry() {
        IssuedRefreshToken issued = RefreshToken.issue(UUID.randomUUID(), Duration.ofMinutes(5));
        assertThat(issued.token().isValid(Instant.now())).isTrue();
    }

    @Test
    void tokenIsInvalidAfterExpiry() {
        IssuedRefreshToken issued = RefreshToken.issue(UUID.randomUUID(), Duration.ofSeconds(-1));
        assertThat(issued.token().isValid(Instant.now())).isFalse();
    }

    @Test
    void revokedTokenIsInvalid() {
        IssuedRefreshToken issued = RefreshToken.issue(UUID.randomUUID(), Duration.ofMinutes(5));
        issued.token().revoke();
        assertThat(issued.token().isValid(Instant.now())).isFalse();
    }

    @Test
    void hashIsDeterministicForSameRawValue() {
        String raw = "some-raw-value";
        assertThat(RefreshToken.hash(raw)).isEqualTo(RefreshToken.hash(raw));
    }

    @Test
    void differentIssuedTokensHaveDifferentRawValues() {
        IssuedRefreshToken a = RefreshToken.issue(UUID.randomUUID(), Duration.ofMinutes(5));
        IssuedRefreshToken b = RefreshToken.issue(UUID.randomUUID(), Duration.ofMinutes(5));
        assertThat(a.rawValue()).isNotEqualTo(b.rawValue());
    }
}
