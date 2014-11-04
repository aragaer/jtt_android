package com.aragaer.jtt.transition;

import java.util.Calendar;
import java.util.TimeZone;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import com.aragaer.jtt.astronomy.DayInterval;


public class DayIntervalCalculator implements DayIntervalFactory {

	private SunriseSunsetCalculator calculator;

	public DayIntervalCalculator(double latitude, double longitude) {
		calculator = new SunriseSunsetCalculator(new Location(latitude,
				longitude), TimeZone.getDefault());
	}

	@Override
	public DayInterval getIntervalForTimestamp(long timestamp) {
		DayInterval result;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timestamp);
		long sunrise = calculator.getOfficialSunriseCalendarForDate(calendar).getTimeInMillis();
		long sunset = calculator.getOfficialSunsetCalendarForDate(calendar).getTimeInMillis();
		if (timestamp < sunrise) {
			calendar.add(Calendar.DATE, -1);
			long previousSunset = calculator.getOfficialSunsetCalendarForDate(calendar).getTimeInMillis();
			result = DayInterval.Night(previousSunset, sunrise);
		} else if (timestamp >= sunset) {
			calendar.add(Calendar.DATE, 1);
			long nextSunrise = calculator.getOfficialSunriseCalendarForDate(calendar).getTimeInMillis();
			result = DayInterval.Night(sunset, nextSunrise);
		} else
			result = DayInterval.Day(sunrise, sunset);
		return result;
	}

}
