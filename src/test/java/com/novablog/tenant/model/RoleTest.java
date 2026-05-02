package com.novablog.tenant.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link Role} enum hierarchy.
 */
@DisplayName("Role Hierarchy")
class RoleTest {

    @Test
    @DisplayName("SUPER_ADMIN should be the highest level")
    void superAdminShouldBeHighest() {
        assertThat(Role.SUPER_ADMIN.getLevel()).isEqualTo(5);
    }

    @Test
    @DisplayName("READER should be the lowest level")
    void readerShouldBeLowest() {
        assertThat(Role.READER.getLevel()).isEqualTo(1);
    }

    @ParameterizedTest
    @CsvSource({
            "SUPER_ADMIN, ORG_ADMIN, true",
            "SUPER_ADMIN, READER, true",
            "ORG_ADMIN, EDITOR, true",
            "EDITOR, WRITER, true",
            "WRITER, READER, true",
            "WRITER, WRITER, true",
            "READER, WRITER, false",
            "WRITER, EDITOR, false",
            "EDITOR, ORG_ADMIN, false",
    })
    @DisplayName("isAtLeast should compare role levels correctly")
    void isAtLeastShouldWork(Role higher, Role lower, boolean expected) {
        assertThat(higher.isAtLeast(lower)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "SUPER_ADMIN, ORG_ADMIN, true",
            "ORG_ADMIN, EDITOR, true",
            "WRITER, WRITER, false",
            "READER, WRITER, false",
    })
    @DisplayName("isAbove should compare strict inequality")
    void isAboveShouldWork(Role higher, Role lower, boolean expected) {
        assertThat(higher.isAbove(lower)).isEqualTo(expected);
    }

    @Test
    @DisplayName("should have exactly 5 roles in the hierarchy")
    void shouldHaveFiveRoles() {
        assertThat(Role.values()).hasSize(5);
    }

    @Test
    @DisplayName("hierarchy order should be SUPER_ADMIN > ORG_ADMIN > EDITOR > WRITER > READER")
    void hierarchyOrderShouldBeCorrect() {
        assertThat(Role.SUPER_ADMIN.getLevel())
                .isGreaterThan(Role.ORG_ADMIN.getLevel());
        assertThat(Role.ORG_ADMIN.getLevel())
                .isGreaterThan(Role.EDITOR.getLevel());
        assertThat(Role.EDITOR.getLevel())
                .isGreaterThan(Role.WRITER.getLevel());
        assertThat(Role.WRITER.getLevel())
                .isGreaterThan(Role.READER.getLevel());
    }
}
