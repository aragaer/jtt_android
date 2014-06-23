package com.aragaer.jtt.transition;

import java.util.Calendar;

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class DayIntervalCalculatorTest {

	private DayIntervalCalculator calculator;

	@Rule
	public TestWithLocation locationAnnotation = new TestWithLocation();

	@Before
	public void setUp() {
		calculator = new DayIntervalCalculator(
				locationAnnotation.getLatitude(),
				locationAnnotation.getLongitude());
	}

	@Test
	@Location(latitude = 55.93, longitude = 37.79)
	public void testMoscowNoon() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2014, 5, 23, 12, 0, 0);
		long noon23Jun2014 = calendar.getTimeInMillis();
		DayInterval interval = calculator
				.getIntervalForTimestamp(noon23Jun2014);
		assertNotNull(interval);
		assertTrue(interval.isDay());
		Sunrise sunrise = (Sunrise) interval.getStart();
		Sunset sunset = (Sunset) interval.getEnd();
		assertThat(sunrise.getTimestamp(), lessThan(noon23Jun2014));
		assertThat(sunset.getTimestamp(), greaterThan(noon23Jun2014));
	}

	@Test
	@Location(latitude = 55.93, longitude = 37.79)
	public void testMoscowMidnight() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2014, 5, 23, 0, 0, 0);
		long midnight23Jun2014 = calendar.getTimeInMillis();
		DayInterval interval = calculator
				.getIntervalForTimestamp(midnight23Jun2014);
		assertFalse(interval.isDay());
		Sunset sunset = (Sunset) interval.getStart();
		Sunrise sunrise = (Sunrise) interval.getEnd();
		assertThat(sunset.getTimestamp(), lessThan(midnight23Jun2014));
		assertThat(sunrise.getTimestamp(), greaterThan(midnight23Jun2014));
	}

	@Test
	@Location(latitude = 55.93, longitude = 37.79)
	public void testMoscowSunrise() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2014, 5, 23, 12, 0, 0);
		long noon23Jun2014 = calendar.getTimeInMillis();
		calendar.set(2014, 5, 23, 0, 0, 0);
		long midnight23Jun2014 = calendar.getTimeInMillis();
		DayInterval day = calculator.getIntervalForTimestamp(noon23Jun2014);
		DayInterval night = calculator
				.getIntervalForTimestamp(midnight23Jun2014);
		assertThat(day.getStart().getTimestamp(), equalTo(night.getEnd()
				.getTimestamp()));
	}

	@Test
	@Location(latitude = 55.93, longitude = 37.79)
	public void testMoscowSunset() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2014, 5, 23, 12, 0, 0);
		long noon23Jun2014 = calendar.getTimeInMillis();
		calendar.set(2014, 5, 24, 0, 0, 0);
		long midnight24Jun2014 = calendar.getTimeInMillis();
		DayInterval day = calculator.getIntervalForTimestamp(noon23Jun2014);
		DayInterval night = calculator
				.getIntervalForTimestamp(midnight24Jun2014);
		assertThat(day.getEnd().getTimestamp(), equalTo(night.getStart()
				.getTimestamp()));
	}

	@Test
	@Location(latitude = 55.93, longitude = 37.79)
	public void testMoscowBeforeSunrise() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2014, 5, 23, 12, 0, 0);
		long noon23Jun2014 = calendar.getTimeInMillis();
		DayInterval day = calculator.getIntervalForTimestamp(noon23Jun2014);
		long sunrise23Jun2014 = day.getStart().getTimestamp();
		DayInterval night = calculator
				.getIntervalForTimestamp(sunrise23Jun2014 - 1);
		assertFalse(night.isDay());
		assertThat(night.getEnd().getTimestamp(), equalTo(sunrise23Jun2014));
	}

	@Test
	@Location(latitude = 55.93, longitude = 37.79)
	public void testMoscowAfterSunset() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2014, 5, 23, 12, 0, 0);
		long noon23Jun2014 = calendar.getTimeInMillis();
		DayInterval day = calculator.getIntervalForTimestamp(noon23Jun2014);
		long sunset23Jun2014 = day.getEnd().getTimestamp();
		DayInterval night = calculator
				.getIntervalForTimestamp(sunset23Jun2014 + 1);
		assertFalse(night.isDay());
		assertThat(night.getStart().getTimestamp(), equalTo(sunset23Jun2014));
	}
}
