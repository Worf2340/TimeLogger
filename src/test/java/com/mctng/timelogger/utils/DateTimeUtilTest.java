package com.mctng.timelogger.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateTimeUtilTest {

    @Test
    void format_millisTest() {
        long millis = 199389183;
        String formatted = DateTimeUtil.formatMillis(millis);
        assertEquals(formatted, "55h 23m 9s");
    }
}