package com.aragaer.jtt.core;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

public class DayIntervalCalculator {

	private double latitude, longitude;
	private TimeZone timezone;
	private SunriseSunsetCalculator calculator;

	public void setLocation(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
		calculator = null;
	}

	private void prepareCalculator() {
		if (calculator == null)
			calculator = new SunriseSunsetCalculator(new Location(latitude, longitude), timezone);
	}

	public DayInterval getDay(long jdn) {
		prepareCalculator();
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(jdnToTimestamp(jdn+1));
		long sunrise = roundToMinute(calculator.getOfficialSunriseForDate(date));
		long sunset = roundToMinute(calculator.getOfficialSunsetForDate(date));
		return new DayInterval(sunrise, sunset, true);
	}

	public DayInterval getNight(long jdn) {
		prepareCalculator();
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(jdnToTimestamp(jdn+1));
		long sunrise = roundToMinute(calculator.getOfficialSunriseForDate(date));
		date.add(Calendar.DATE, -1);
		long sunset = roundToMinute(calculator.getOfficialSunsetForDate(date));
		return new DayInterval(sunset, sunrise, false);
	}

	private long roundToMinute(long value) {
		return 60000 * Math.round(value / 60000.0);
	}

	private long jdnToTimestamp(long jdn) {
		return Math.round(((double) jdn - 2440587.5) * TimeUnit.SECONDS
				.toMillis(60 * 60 * 24));
	}

	public void setTimezone(TimeZone timeZone) {
		this.timezone = timeZone;
		calculator = null;
	}
}
