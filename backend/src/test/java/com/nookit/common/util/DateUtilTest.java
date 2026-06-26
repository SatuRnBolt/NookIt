package com.nookit.common.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class DateUtilTest {

    @Test
    void nowAndTodayAreNotNull() {
        assertNotNull(DateUtil.now());
        assertNotNull(DateUtil.today());
    }

    @Test
    void formatsDateTime() {
        LocalDateTime dt = LocalDateTime.of(2026, 6, 26, 9, 8, 7);
        assertEquals("2026-06-26 09:08:07", DateUtil.format(dt));
    }

    @Test
    void formatsDate() {
        assertEquals("2026-06-26", DateUtil.format(LocalDate.of(2026, 6, 26)));
    }

    @Test
    void parsesDateTimeRoundTrip() {
        LocalDateTime dt = LocalDateTime.of(2026, 6, 26, 9, 8, 7);
        assertEquals(dt, DateUtil.parseDateTime(DateUtil.format(dt)));
    }

    @Test
    void parsesDateRoundTrip() {
        LocalDate d = LocalDate.of(2026, 6, 26);
        assertEquals(d, DateUtil.parseDate(DateUtil.format(d)));
    }

    @Test
    void nullInputsReturnNull() {
        assertNull(DateUtil.format((LocalDateTime) null));
        assertNull(DateUtil.format((LocalDate) null));
        assertNull(DateUtil.parseDateTime(null));
        assertNull(DateUtil.parseDate(null));
    }
}
