package com.github.wujichen158.ikakuji.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class TimeUtil {
    private static final DateTimeFormatter SIMPLE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Parse time string to LocalDateTime
     *
     * @param timeString
     * @return
     */
    public static LocalDateTime parseTimeString(String timeString) {
        try {
            return LocalDateTime.parse(timeString, SIMPLE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Judge whether the given end time is already passed
     *
     * @param endTime
     * @return
     */
    public static boolean isEndTimePassed(String endTime) {
        return !endTime.isBlank() && Optional.ofNullable(parseTimeString(endTime))
                .map(endTimeDate -> endTimeDate.isBefore(LocalDateTime.now()))
                .orElse(true);
    }

    /**
     * Parse LocalDateTime to time string
     *
     * @param localDateTime
     * @return
     */
    public static String parseDateTime(LocalDateTime localDateTime) {
        return localDateTime.format(SIMPLE_FORMATTER);
    }

    /**
     * Format LocalDateTime to full time string
     *
     * @param localDateTime
     * @return
     */
    public static String formatDateTime(LocalDateTime localDateTime) {
        return localDateTime.format(FORMATTER);
    }
}
