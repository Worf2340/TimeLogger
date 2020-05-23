package com.mctng.timelogger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SQLiteTest {

    private SQLite sqlHandler;
    private String uuid = "b84f4243-ac71-44e9-83cb-cdbfcd15235a";

    @BeforeEach
    void setUp() {
        sqlHandler = new SQLite("C:\\Users\\vedan\\Documents\\Minecraft Modding\\Fin Plugins\\" +
                "TimeLogger Server - 1.7.10\\plugins\\TimeLogger");
        sqlHandler.clearDB();
    }

    @Test
    void getPlayTime_oneRecord_54000000() {
        sqlHandler.insertPlayerTimeLogger(uuid, 54000000, "2020-05-22 07:00:00", "2020-05-22 22:00:00");
        assertEquals(54000000, sqlHandler.getPlaytime(uuid));
    }

    @Test
    void getPlayTime_twoRecords_108000000() {
        sqlHandler.insertPlayerTimeLogger(uuid, 54000000, "2020-05-22 07:00:00", "2020-05-22 22:00:00");
        sqlHandler.insertPlayerTimeLogger(uuid, 54000000, "2020-05-20 07:00:00", "2020-05-20 22:00:00");

        assertEquals(108000000, sqlHandler.getPlaytime(uuid));
    }

    @Test
    void getPlayTimeBetweenTimes_oneRecordStartingBeforeQueryEndingBeforeQuery_3600000() {
        sqlHandler.insertPlayerTimeLogger(uuid, 54000000, "2020-05-22 07:00:00", "2020-05-22 22:00:00");
        assertEquals(3600000, sqlHandler.getPlaytimeBetweenTimes(uuid,
                "2019-05-22 06:00:00", "2020-05-22 08:00:00"));
    }

    @Test
    void getPlayTimeBetweenTimes_oneRecordStartingBeforeQueryEndingEqualsQuery_54000000() {
        sqlHandler.insertPlayerTimeLogger(uuid, 54000000, "2020-05-22 07:00:00", "2020-05-22 22:00:00");
        assertEquals(54000000, sqlHandler.getPlaytimeBetweenTimes(uuid,
                "2020-05-22 06:00:00", "2020-05-22 22:00:00"));
    }

    @Test
    void getPlayTimeBetweenTimes_oneRecordStartingBeforeQueryEndingAfterQuery_54000000() {
        sqlHandler.insertPlayerTimeLogger(uuid, 54000000, "2020-05-22 07:00:00", "2020-05-22 22:00:00");
        assertEquals(54000000, sqlHandler.getPlaytimeBetweenTimes(uuid,
                "2020-05-22 06:00:00", "2020-05-22 23:00:00"));
    }

    @Test
    void getPlayTimeBetweenTimes_oneRecordStartingEqualsQueryEndingBeforeQuery_10800000() {
        sqlHandler.insertPlayerTimeLogger(uuid, 54000000, "2020-05-22 07:00:00", "2020-05-22 22:00:00");
        assertEquals(10800000, sqlHandler.getPlaytimeBetweenTimes(uuid,
                "2020-05-22 07:00:00", "2020-05-22 10:00:00"));
    }

    @Test
    void getPlayTimeBetweenTimes_oneRecordStartingEqualsQueryEndingEqualsQuery_54000000() {
        sqlHandler.insertPlayerTimeLogger(uuid, 54000000, "2020-05-22 07:00:00", "2020-05-22 22:00:00");
        assertEquals(54000000, sqlHandler.getPlaytimeBetweenTimes(uuid,
                "2020-05-22 07:00:00", "2020-05-22 22:00:00"));
    }

    @Test
    void getPlayTimeBetweenTimes_oneRecordStartingEqualsQueryEndingAfterQuery_54000000() {
        sqlHandler.insertPlayerTimeLogger(uuid, 54000000, "2020-05-22 07:00:00", "2020-05-22 22:00:00");
        assertEquals(54000000, sqlHandler.getPlaytimeBetweenTimes(uuid,
                "2020-05-22 07:00:00", "2020-05-22 23:00:00"));
    }

    @Test
    void getPlayTimeBetweenTimes_oneRecordStartingAfterQueryEndingBeforeQuery_3600000() {
        sqlHandler.insertPlayerTimeLogger(uuid, 54000000, "2020-05-22 07:00:00", "2020-05-22 22:00:00");
        assertEquals(3600000, sqlHandler.getPlaytimeBetweenTimes(uuid,
                "2020-05-22 20:00:00", "2020-05-22 21:00:00"));
    }

    @Test
    void getPlayTimeBetweenTimes_oneRecordStartingAfterQueryEndingEqualsQuery_7200000() {
        sqlHandler.insertPlayerTimeLogger(uuid, 54000000, "2020-05-22 07:00:00", "2020-05-22 22:00:00");
        assertEquals(7200000, sqlHandler.getPlaytimeBetweenTimes(uuid,
                "2020-05-22 20:00:00", "2020-05-22 22:00:00"));
    }

    @Test
    void getPlayTimeBetweenTimes_oneRecordStartingAfterQueryEndingAfterQuery_7200000() {
        sqlHandler.insertPlayerTimeLogger(uuid, 54000000, "2020-05-22 07:00:00", "2020-05-22 22:00:00");
        assertEquals(7200000, sqlHandler.getPlaytimeBetweenTimes(uuid,
                "2020-05-22 20:00:00", "2020-05-22 23:00:00"));
    }


}