package com.fiapx.identity.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fiapx.identity.domain.model.Email;
import org.junit.jupiter.api.Test;

class EmailTest {

    @Test
    void normalizesToLowerCaseAndTrims() {
        Email email = Email.of("  User@Example.COM  ");
        assertThat(email.value()).isEqualTo("user@example.com");
    }

    @Test
    void rejectsInvalidFormat() {
        assertThatThrownBy(() -> Email.of("not-an-email"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlank() {
        assertThatThrownBy(() -> Email.of(" ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equalityIsCaseInsensitiveByNormalizedValue() {
        assertThat(Email.of("a@b.com")).isEqualTo(Email.of("A@B.com"));
    }
}
