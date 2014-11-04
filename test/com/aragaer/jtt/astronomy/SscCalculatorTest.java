package com.aragaer.jtt.astronomy;
// vim: et ts=4 sts=4 sw=4

import java.util.Calendar;

import org.junit.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.aragaer.jtt.test.*;


public class SscCalculatorTest {

    private DayIntervalCalculator calculator;

    @Rule
    public TestWithLocation locationAnnotation = new TestWithLocation();

    @Before
    public void setUp() {
        calculator = new SscCalculator();
        calculator.setLocation(locationAnnotation.getLocation());
    }

    @Test
    @TestLocation(latitude=55.93, longitude=37.79)
    public void testMoscowNoon() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, 5, 23, 12, 0, 0);
        long noon23Jun2014 = calendar.getTimeInMillis();

        DayInterval interval = calculator.getIntervalFor(noon23Jun2014);

        assertNotNull(interval);
        assertTrue(interval.isDay());
        assertThat(interval.getStart(), lessThan(noon23Jun2014));
        assertThat(interval.getEnd(), greaterThan(noon23Jun2014));
    }

    @Test
    @TestLocation(latitude=55.93, longitude=37.79)
    public void testMoscowMidnight() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, 5, 23, 0, 0, 0);
        long midnight23Jun2014 = calendar.getTimeInMillis();

        DayInterval interval = calculator.getIntervalFor(midnight23Jun2014);

        assertFalse(interval.isDay());
        assertThat(interval.getStart(), lessThan(midnight23Jun2014));
        assertThat(interval.getEnd(), greaterThan(midnight23Jun2014));
    }

    @Test
    @TestLocation(latitude = 55.93, longitude = 37.79)
    public void testMoscowSunrise() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, 5, 23, 12, 0, 0);
        long noon23Jun2014 = calendar.getTimeInMillis();
        calendar.set(2014, 5, 23, 0, 0, 0);
        long midnight23Jun2014 = calendar.getTimeInMillis();
        DayInterval day = calculator.getIntervalFor(noon23Jun2014);
        DayInterval night = calculator.getIntervalFor(midnight23Jun2014);
        assertThat(day.getStart(), equalTo(night.getEnd()));
    }

    @Test
    @TestLocation(latitude = 55.93, longitude = 37.79)
    public void testMoscowSunset() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, 5, 23, 12, 0, 0);
        long noon23Jun2014 = calendar.getTimeInMillis();
        calendar.set(2014, 5, 24, 0, 0, 0);
        long midnight24Jun2014 = calendar.getTimeInMillis();
        DayInterval day = calculator.getIntervalFor(noon23Jun2014);
        DayInterval night = calculator.getIntervalFor(midnight24Jun2014);
        assertThat(day.getEnd(), equalTo(night.getStart()));
    }

    @Test
    @TestLocation(latitude = 55.93, longitude = 37.79)
    public void testMoscowBeforeSunrise() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, 5, 23, 12, 0, 0);
        long noon23Jun2014 = calendar.getTimeInMillis();
        DayInterval day = calculator.getIntervalFor(noon23Jun2014);
        long sunrise23Jun2014 = day.getStart();
        DayInterval night = calculator.getIntervalFor(sunrise23Jun2014 - 1);
        assertFalse(night.isDay());
        assertThat(night.getEnd(), equalTo(sunrise23Jun2014));
    }

    @Test
    @TestLocation(latitude = 55.93, longitude = 37.79)
    public void testMoscowAfterSunset() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, 5, 23, 12, 0, 0);
        long noon23Jun2014 = calendar.getTimeInMillis();
        DayInterval day = calculator.getIntervalFor(noon23Jun2014);
        long sunset23Jun2014 = day.getEnd();
        DayInterval night = calculator.getIntervalFor(sunset23Jun2014 + 1);
        assertFalse(night.isDay());
        assertThat(night.getStart(), equalTo(sunset23Jun2014));
    }
}
