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
}
