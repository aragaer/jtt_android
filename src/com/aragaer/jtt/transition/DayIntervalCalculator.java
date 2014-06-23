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
		DayInterval result;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timestamp);
		long sunrise = calculator.getOfficialSunriseCalendarForDate(calendar).getTimeInMillis();
		long sunset = calculator.getOfficialSunsetCalendarForDate(calendar).getTimeInMillis();
		if (timestamp < sunrise) {
			calendar.add(Calendar.DATE, -1);
			long previousSunset = calculator.getOfficialSunsetCalendarForDate(calendar).getTimeInMillis();
			result = new Night(new Sunset(previousSunset), new Sunrise(sunrise));
		} else if (timestamp >= sunset) {
			calendar.add(Calendar.DATE, 1);
			long nextSunrise = calculator.getOfficialSunriseCalendarForDate(calendar).getTimeInMillis();
			result = new Night(new Sunset(sunset), new Sunrise(nextSunrise));
		} else
			result = new Day(new Sunrise(sunrise), new Sunset(sunset));
		return result;
	}

}
