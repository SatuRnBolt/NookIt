package com.nookit.common.util;

import com.nookit.common.constant.CommonConstants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 基于 {@code java.time} 的日期工具，统一使用 {@link CommonConstants#DEFAULT_TIMEZONE}。
 */
public final class DateUtil {

    public static final ZoneId ZONE = ZoneId.of(CommonConstants.DEFAULT_TIMEZONE);

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(CommonConstants.DEFAULT_DATE_PATTERN);
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(CommonConstants.DEFAULT_DATETIME_PATTERN);

    private DateUtil() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE);
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATETIME_FORMATTER);
    }

    public static String format(LocalDate date) {
        return date == null ? null : date.format(DATE_FORMATTER);
    }

    public static LocalDateTime parseDateTime(String text) {
        return text == null ? null : LocalDateTime.parse(text, DATETIME_FORMATTER);
    }

    public static LocalDate parseDate(String text) {
        return text == null ? null : LocalDate.parse(text, DATE_FORMATTER);
    }
}
