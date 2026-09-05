package com.sunrisedental.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class PasswordUtilTest {

    @Test
    void shouldRejectBlankPassword() {

        assertThrows(
                IllegalArgumentException.class,
                () -> PasswordUtil.hash("")
        );
    }
}