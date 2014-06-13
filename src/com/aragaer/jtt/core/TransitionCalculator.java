package com.aragaer.jtt.core;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

public class TransitionCalculator {
	public static final long ms_per_day = TimeUnit.SECONDS
			.toMillis(60 * 60 * 24);
	final Map<Long, long[]> cache = new HashMap<Long, long[]>();
	SunriseSunsetCalculator calculator;

	boolean calculateTransitions(long now, long[] tr) {
		if (calculator == null)
			throw new IllegalStateException("Location not set");

		long jdn = TransitionCalculator.longToJDN(now);
		boolean is_day = true;
		tr[0] = getTrForJDN(jdn - 1)[1];
		tr[1] = getTrForJDN(jdn)[0];
		tr[2] = getTrForJDN(jdn)[1];
		tr[3] = getTrForJDN(jdn + 1)[0];

		// if tr2 is before now
		while (now >= tr[2]) {
			for (int i = 0; i < 3; i++)
				tr[i] = tr[i + 1];
			if (is_day)
				tr[3] = getTrForJDN(jdn + 1)[1];
			else {
				jdn++;
				tr[3] = getTrForJDN(jdn + 1)[0];
			}
			is_day = !is_day;
		}

		// (else) if tr1 is after now
		while (now < tr[1]) {
			for (int i = 0; i < 3; i++)
				tr[i + 1] = tr[i];
			if (is_day)
				tr[0] = getTrForJDN(jdn - 1)[0];
			else {
				jdn--;
				tr[0] = getTrForJDN(jdn - 1)[1];
			}
			is_day = !is_day;
		}

		return is_day;
	}

	private long[] getTrForJDN(final long jdn) {
		long[] result = cache.get(jdn);
		if (result == null) {
			final Calendar date = Calendar.getInstance();
			date.setTimeInMillis(TransitionCalculator.JDToLong(jdn));
			result = new long[] { calculator.getOfficialSunriseForDate(date),
					calculator.getOfficialSunsetForDate(date) };
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
