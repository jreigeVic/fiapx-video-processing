package com.fiapx.processing.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fiapx.processing.domain.model.FrameCount;
import org.junit.jupiter.api.Test;

class FrameCountTest {

    @Test
    void acceptsNonNegativeValues() {
        assertThat(FrameCount.of(0).value()).isZero();
        assertThat(FrameCount.of(120).value()).isEqualTo(120);
    }

    @Test
    void rejectsNegativeValues() {
        assertThatThrownBy(() -> FrameCount.of(-1)).isInstanceOf(IllegalArgumentException.class);
    }
}
