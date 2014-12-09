package com.aragaer.jtt.astronomy;
// vim: et ts=4 sts=4 sw=4

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.junit.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.aragaer.jtt.test.*;


public class SscCalculatorTest {

    private SscCalculator calculator;
    private long timestamp;
    private TimeZone tz;

    @Rule
    public TestWithTz tzAnnotation = new TestWithTz();

    @Rule
    public TestWithLocation locationAnnotation = new TestWithLocation();

    @Rule
    public TestWithTime timeAnnotation = new TestWithTime();

    @Before
    public void setUp() {
        tz = tzAnnotation.getTimeZone();
        TimeZone.setDefault(tz);
        timestamp = timeAnnotation.getTimestamp();
        calculator = new SscCalculator();
        calculator.setLocation(locationAnnotation.getLocation());
    }

    @Test
    @TestTimezone(offsetMinutes = 180)
    @TestLocation(latitude=55.93, longitude=37.79)
    @TestTime(year=2014, month=5, day=23, hour=12)
    public void testMoscowNoon() {
        DayInterval interval = calculator.getIntervalFor(timestamp);

        assertNotNull(interval);
        assertTrue(interval.isDay());
        assertThat(interval.getStart(), lessThan(timestamp));
        assertThat(interval.getEnd(), greaterThan(timestamp));
    }

    @Test
    @TestTimezone(offsetMinutes = 180)
    @TestLocation(latitude=55.93, longitude=37.79)
    @TestTime(year=2014, month=5, day=23, hour=0)
    public void testMoscowMidnight() {
        DayInterval interval = calculator.getIntervalFor(timestamp);

        assertFalse(interval.isDay());
        assertThat(interval.getStart(), lessThan(timestamp));
        assertThat(interval.getEnd(), greaterThan(timestamp));
    }

    @Test
    @TestTimezone(offsetMinutes = 180)
    @TestLocation(latitude = 55.93, longitude = 37.79)
    @TestTime(year=2014, month=5, day=23, hour=0)
    public void testMoscowSunrise() {
        DayInterval night = calculator.getIntervalFor(timestamp);
        DayInterval day = calculator.getIntervalFor(timestamp + TimeUnit.HOURS.toMillis(12));
        assertThat(day.getStart(), equalTo(night.getEnd()));
    }

    @Test
    @TestTimezone(offsetMinutes = 180)
    @TestLocation(latitude = 55.93, longitude = 37.79)
    @TestTime(year=2014, month=5, day=23, hour=12)
    public void testMoscowSunset() {
        DayInterval day = calculator.getIntervalFor(timestamp);
        DayInterval night = calculator.getIntervalFor(timestamp + TimeUnit.HOURS.toMillis(12));
        assertThat(day.getEnd(), equalTo(night.getStart()));
    }

    @Test
    @TestTimezone(offsetMinutes = 180)
    @TestLocation(latitude = 55.93, longitude = 37.79)
    @TestTime(year=2014, month=5, day=23, hour=12)
    public void testMoscowBeforeSunrise() {
        DayInterval day = calculator.getIntervalFor(timestamp);
        DayInterval night = calculator.getIntervalFor(day.getStart() - 1);
        assertFalse(night.isDay());
        assertThat(night.getEnd(), equalTo(day.getStart()));
    }

    @Test
    @TestTimezone(offsetMinutes = 180)
    @TestLocation(latitude = 55.93, longitude = 37.79)
    @TestTime(year=2014, month=5, day=23, hour=12)
    public void testMoscowAfterSunset() {
        DayInterval day = calculator.getIntervalFor(timestamp);
        DayInterval night = calculator.getIntervalFor(day.getEnd() + 1);
        assertFalse(night.isDay());
        assertThat(night.getStart(), equalTo(day.getEnd()));
    }

    @Test
    @TestTimezone(offsetMinutes=0)
    @TestLocation(latitude=51.5, longitude=0.0)
    @TestTime(year=2000, month=0, day=1, hour=12)
    public void testLondonDay01Jan2000() {
        DayInterval day = calculator.getIntervalFor(timestamp);
        assertTrue(day.isDay());
        checkValues(day, "08:06", "16:02");
        checkValues(day, dateToTimestamp(2000, 0, 1, 8, 6), dateToTimestamp(2000, 0, 1, 16, 2));
    }

    @Test
    @TestTimezone(offsetMinutes=0)
    @TestLocation(latitude=51.5, longitude=0.0)
    @TestTime(year=2000, month=0, day=1)
    public void testLondonNight01Jan2000() {
        DayInterval night = calculator.getIntervalFor(timestamp);
        assertFalse(night.isDay());
        checkValues(night, "16:00", "08:06");
        checkValues(night, dateToTimestamp(1999, 11, 31, 16, 0), dateToTimestamp(2000, 0, 1, 8, 6));
    }

    @Test
    @TestTimezone(offsetMinutes = -60)
    @TestLocation(latitude=15.11, longitude=-23.6)
    @TestTime(year=2000, month=0, day=1, hour=12)
    public void testCapeVerdeDay01Jan2000() {
        DayInterval day = calculator.getIntervalFor(timestamp);
        assertTrue(day.isDay());
        checkValues(day, "07:00", "18:16");
        checkValues(day, dateToTimestamp(2000, 0, 1, 7, 0), dateToTimestamp(2000, 0, 1, 18, 16));
    }

    @Test
    @TestTimezone(offsetMinutes = 180)
    @TestLocation(latitude=55.93, longitude=37.79)
    @TestTime(year=2000, month=0, day=1, hour=12)
    public void testMoscowDay01Jan2000() {
        DayInterval day = calculator.getIntervalFor(timestamp);
        assertTrue(day.isDay());
        checkValues(day, "09:00", "16:05");
        checkValues(day, dateToTimestamp(2000, 0, 1, 9, 00), dateToTimestamp(2000, 0, 1, 16, 05));
    }

    @Test
    @TestTimezone(offsetMinutes = 180)
    @TestLocation(latitude=55.93, longitude=37.79)
    @TestTime(year=2014, month=5, day=22, hour=12)
    public void testMoscowDay22Jun2014() {
        DayInterval day = calculator.getIntervalFor(timestamp);
        assertTrue(day.isDay());
        checkValues(day, "03:43", "21:19");
        checkValues(day, dateToTimestamp(2014, 5, 22, 3, 43), dateToTimestamp(2014, 5, 22, 21, 19));
    }

    private void checkValues(DayInterval interval, long start, long end) {
        assertThat(interval.getStart(), equalTo(start));
        assertThat(interval.getEnd(), equalTo(end));
    }

    private void checkValues(DayInterval interval, String start, String end) {
        assertThat(timestampToLocaltime(interval.getStart()), equalTo(start));
        assertThat(timestampToLocaltime(interval.getEnd()), equalTo(end));
    }

    private long dateToTimestamp(int year, int month, int day) {
        return dateToTimestamp(year, month, day, 0, 0);
    }

    private long dateToTimestamp(int year, int month, int day, int hour,
            int minute) {
        Calendar calendar = Calendar.getInstance(tz);
        calendar.set(year, month, day, hour, minute, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private String timestampToLocaltime(long timestamp) {
        Calendar calendar = Calendar.getInstance(tz);
        calendar.setTimeInMillis(timestamp);
        return String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));
    }

}
