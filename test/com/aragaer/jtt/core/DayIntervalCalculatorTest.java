package com.aragaer.jtt.core;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.aragaer.jtt.astronomy.DayInterval;


@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class DayIntervalCalculatorTest {

	private DayIntervalCalculator calculator;
	private TimeZone tz;

	@Rule
	public TestWithTz tzAnnotation = new TestWithTz();

	@Before
	public void setup() {
		calculator = new DayIntervalCalculator();
		tz = tzAnnotation.getTimeZone();
		calculator.setTimezone(tz);
	}

	@Test
	@Timezone(offsetMinutes = 0)
	public void testLondonDay01Jan2000() {
		calculator.setLocation(51.5, 0);
		DayInterval day = calculator.getDay(calendarToJDN(dateToTimestamp(2000,
				0, 1)));
		assertTrue(day.isDay());
		checkValues(day, "08:06", "16:02");
		checkValues(day, dateToTimestamp(2000, 0, 1, 8, 6),
				dateToTimestamp(2000, 0, 1, 16, 2));
	}

	@Test
	@Timezone(offsetMinutes = 0)
	public void testLondonNight01Jan2000() {
		calculator.setLocation(51.5, 0);
		DayInterval night = calculator.getNight(calendarToJDN(dateToTimestamp(
				2000, 0, 1)));
		assertFalse(night.isDay());
		checkValues(night, "16:00", "08:06");
		checkValues(night, dateToTimestamp(1999, 11, 31, 16, 0),
				dateToTimestamp(2000, 0, 1, 8, 6));
	}

	@Test
	@Timezone(offsetMinutes = -60)
	public void testCapeVerdeDay01Jan2000() {
		calculator.setLocation(15.11, -23.6);
		DayInterval day = calculator.getDay(calendarToJDN(dateToTimestamp(2000,
				0, 1)));
		assertTrue(day.isDay());
		checkValues(day, "07:00", "18:16");
		checkValues(day, dateToTimestamp(2000, 0, 1, 7, 0),
				dateToTimestamp(2000, 0, 1, 18, 16));
	}

	@Test
	@Timezone(offsetMinutes = 180)
	public void testMoscowDay01Jan2000() {
		calculator.setLocation(55.93, 37.79);
		DayInterval day = calculator.getDay(calendarToJDN(dateToTimestamp(2000,
				0, 1)));
		assertTrue(day.isDay());
		checkValues(day, "09:00", "16:05");
		checkValues(day, dateToTimestamp(2000, 0, 1, 9, 00),
				dateToTimestamp(2000, 0, 1, 16, 05));
	}

	@Test
	@Timezone(offsetMinutes = 180)
	public void testMoscowDay22Jun2014() {
		calculator.setLocation(55.93, 37.79);
		DayInterval day = calculator.getDay(calendarToJDN(dateToTimestamp(2014,
				5, 22)));
		assertTrue(day.isDay());
		checkValues(day, "03:43", "21:19");
		checkValues(day, dateToTimestamp(2014, 5, 22, 3, 43),
				dateToTimestamp(2014, 5, 22, 21, 19));
	}

	private void checkValues(DayInterval interval, long start, long end) {
		assertThat(interval.getStart(), equalTo(start));
		assertThat(interval.getEnd(), equalTo(end));
	}

	private void checkValues(DayInterval interval, String start, String end) {
		assertThat(timestampToLocaltime(interval.getStart()), equalTo(start));
		assertThat(timestampToLocaltime(interval.getEnd()), equalTo(end));
	}

	private final double JDN_OFFSET = 2440587.5;

	private long calendarToJDN(long timestamp) {
		return (long) Math.floor(timestamp
				/ ((double) TimeUnit.DAYS.toMillis(1)) + JDN_OFFSET);
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
