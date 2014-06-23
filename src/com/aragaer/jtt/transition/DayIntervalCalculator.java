package com.aragaer.jtt.transition;

import java.util.Calendar;
import java.util.TimeZone;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

public class DayIntervalCalculator implements DayIntervalFactory {

	private SunriseSunsetCalculator calculator;

	public DayIntervalCalculator(double latitude, double longitude) {
		calculator = new SunriseSunsetCalculator(new Location(latitude,
				longitude), TimeZone.getDefault());
	}

	@Override
	public DayInterval getIntervalForTimestamp(long timestamp) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timestamp);
		long sunrise = calculator.getOfficialSunriseCalendarForDate(calendar).getTimeInMillis();
		long sunset = calculator.getOfficialSunsetCalendarForDate(calendar).getTimeInMillis();
		if (timestamp >= sunrise && timestamp < sunset)
			return new Day(new Sunrise(sunrise), new Sunset(sunset));
		else
			return new Night(new Sunset(timestamp - 1), new Sunrise(
					timestamp + 1));
	}

}
