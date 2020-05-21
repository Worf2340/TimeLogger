package com.mctng.timelogger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TimeLoggerPlayerTest {

    @BeforeEach
    void setUp() {

    }

    @Test
    void test1() {
        TimeLoggerPlayer player = new TimeLoggerPlayer("Worf2340", new TimeLogger());
        long x = player.getTotalPlayTimeInMillis();
        assert x != 0;
    }

}