package com.aragaer.jtt;
// vim: et ts=4 sts=4 sw=4

import java.util.Calendar;

import org.junit.*;
import org.junit.runner.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.aragaer.jtt.test.*;


public class SunriseSunsetAdapterTest {

	private DayIntervalCalculator calculator;

	@Rule
	public TestWithLocation locationAnnotation = new TestWithLocation();

    @Test
    @TestLocation(latitude = 55.93, longitude = 37.79)
    public void testMoscowNoon() {
    }
}
