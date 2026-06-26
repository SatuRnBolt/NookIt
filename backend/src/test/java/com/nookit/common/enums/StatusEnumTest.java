package com.nookit.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusEnumTest {

    @Test
    void codesMatchExpectedValues() {
        assertEquals("active", StatusEnum.ACTIVE.getCode());
        assertEquals("inactive", StatusEnum.INACTIVE.getCode());
        assertEquals("suspended", StatusEnum.SUSPENDED.getCode());
        assertEquals("locked", StatusEnum.LOCKED.getCode());
    }

    @Test
    void everyValueHasCodeAndDesc() {
        for (StatusEnum status : StatusEnum.values()) {
            assertTrue(status.getCode() != null && !status.getCode().isBlank());
            assertTrue(status.getDesc() != null && !status.getDesc().isBlank());
        }
    }

    @Test
    void isBaseEnum() {
        BaseEnum<String> baseEnum = StatusEnum.ACTIVE;
        assertEquals("active", baseEnum.getCode());
    }
}
