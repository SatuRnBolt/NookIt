package com.nookit.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {

    @Test
    void encodedHashDiffersFromRawAndMatches() {
        String raw = "Secret123!";
        String encoded = PasswordUtil.encode(raw);

        assertNotEquals(raw, encoded);
        assertTrue(PasswordUtil.matches(raw, encoded));
    }

    @Test
    void wrongPasswordDoesNotMatch() {
        String encoded = PasswordUtil.encode("Secret123!");
        assertFalse(PasswordUtil.matches("wrong-password", encoded));
    }

    @Test
    void nullArgumentsDoNotMatch() {
        String encoded = PasswordUtil.encode("Secret123!");
        assertFalse(PasswordUtil.matches(null, encoded));
        assertFalse(PasswordUtil.matches("Secret123!", null));
        assertFalse(PasswordUtil.matches(null, null));
    }
}
