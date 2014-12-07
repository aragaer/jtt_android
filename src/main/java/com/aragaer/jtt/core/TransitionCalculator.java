package com.aragaer.jtt.core;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import com.aragaer.jtt.astronomy.DayInterval;


public class TransitionCalculator {
	public static final long ms_per_day = TimeUnit.SECONDS
			.toMillis(60 * 60 * 24);
	final Map<Long, DayInterval> cache = new HashMap<Long, DayInterval>();
	SunriseSunsetCalculator calculator;

	public ThreeIntervals calculateTransitions(long now) {
		if (calculator == null)
			throw new IllegalStateException("Location not set");

		long jdn = TransitionCalculator.longToJDN(now);
		DayInterval yesterday = getDayForJDN(jdn - 1);
		DayInterval today = getDayForJDN(jdn);
		DayInterval tomorrow = getDayForJDN(jdn + 1);
		ThreeIntervals result = new ThreeIntervals(yesterday.getEnd(),
				today.getStart(), today.getEnd(), tomorrow.getStart(), true);

		while (now >= result.getCurrentEnd())
			if (result.isDayCurrently())
				result = result.shiftToFuture(tomorrow.getEnd());
			else {
				jdn++;
				tomorrow = getDayForJDN(jdn + 1);
				result = result.shiftToFuture(tomorrow.getStart());
			}

		while (now < result.getCurrentStart())
			if (result.isDayCurrently())
				result = result.shiftToPast(yesterday.getStart());
			else {
				jdn--;
				yesterday = getDayForJDN(jdn - 1);
				result = result.shiftToPast(yesterday.getEnd());
			}

		return result;
	}

	private DayInterval getDayForJDN(final long jdn) {
		DayInterval result = cache.get(jdn);
		if (result == null) {
			final Calendar date = Calendar.getInstance();
			date.setTimeInMillis(TransitionCalculator.JDToLong(jdn));
			result = DayInterval.Day(calculator.getOfficialSunriseCalendarForDate(date).getTimeInMillis(),
					calculator.getOfficialSunsetCalendarForDate(date).getTimeInMillis());
			cache.put(jdn, result);
		}
		return result;
	}

	private static long longToJDN(long time) {
		return (long) Math.floor(longToJD(time));
	}

	private static double longToJD(long time) {
		return time / ((double) TransitionCalculator.ms_per_day) + 2440587.5;
	}

	private static long JDToLong(final double jd) {
		return Math.round((jd - 2440587.5) * TransitionCalculator.ms_per_day);
	}

	public void setLocation(float latitude, float longitude) {
		calculator = new SunriseSunsetCalculator(new Location(latitude,
				longitude), TimeZone.getDefault());
		cache.clear();
	}
}
