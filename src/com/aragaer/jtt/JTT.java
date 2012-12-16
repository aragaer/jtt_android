package com.aragaer.jtt;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import android.support.v4.util.LongSparseArray;

public class JTT {
	LongSparseArray<long[]> cache = new LongSparseArray<long[]>();
	SunriseSunsetCalculator calculator;

	public JTT(float lat, float lon) {
		move(lat, lon);
	}

	public void move(float lat, float lon) {
		calculator = new SunriseSunsetCalculator(new Location(lat, lon), TimeZone.getDefault());
		cache.clear();
	}

	static final long ms_per_minute = TimeUnit.SECONDS.toMillis(60);
	static final long ms_per_hour = TimeUnit.SECONDS.toMillis(60 * 60);
	static final long ms_per_day = TimeUnit.SECONDS.toMillis(60 * 60 * 24);

	private final static int HOUR_PARTS = JTTHour.QUARTERS * JTTHour.PARTS;
	private final static int TOTAL_PARTS = HOUR_PARTS * 6;

	public JTTHour time_to_jtt(Calendar c) {
		return time_to_jtt(c == null ? System.currentTimeMillis() : c.getTimeInMillis());
	}

	// wrapped jtt is jtt written as a single integer from range [0; 4800)
	public int time_to_jtt_wrapped(long time) {
		long day = longToJDN(time);
		long tr[] = computeTr(day);
		int dayAdd = 0;
		if (time > tr[1]) // "day" is actually yesterday
			tr = computeTr(++day);

		long tr0 = tr[0], tr1 = tr[1]; // do not modify array contents!

		if (time < tr0) { // before sunrise. Get prev sunset (from cache apparently)
			tr1 = tr0;
			tr0 = computeTr(day - 1)[1];
		} else if (time > tr1) { // after sunset. Get next sunrise
			tr0 = tr1;
			tr1 = computeTr(day + 1)[0];
		} else {
			dayAdd = TOTAL_PARTS;
		}
		return time_tr_to_jtt_wrapped(tr0, tr1, time) + dayAdd;
	}

	// helper function,
	// accepts two transition times and "current" time
	// returns jtt as a single integer from range [0; TOTAL_PARTS)
	// add TOTAL_PARTS for day hours
	// assumes tr0 <= current < tr1
	public static int time_tr_to_jtt_wrapped(long tr0, long tr1, long now) {
		return (int) (TOTAL_PARTS * (now - tr0) / (tr1 - tr0));
	}

	public static JTTHour unwrap_jtt(int wrapped) {
		final int h = wrapped / HOUR_PARTS;
		wrapped %= HOUR_PARTS;
		return new JTTHour(h, wrapped / JTTHour.PARTS, wrapped % JTTHour.PARTS);
	}

	public JTTHour time_to_jtt(long time) {
		return unwrap_jtt(time_to_jtt_wrapped(time));
	}

	// helper function,
	// accepts two transition times and "current" wrapped jtt
	// returns timestamp from [tr0; tr1) appropriate for jtt
	// assumes jtt falls into interval
	public static long wrapped_tr_to_time(long tr0, long tr1, int wrapped) {
		return tr0 + (tr1 - tr0) * wrapped / TOTAL_PARTS;
	}

	public Calendar jtt_to_time(JTTHour hour, Calendar cal) {
		return jtt_to_time(hour.num, hour.quarter, hour.quarter_parts, cal);
	}

	public Calendar jtt_to_time(int n, int q, int f, Calendar cal) {
		return jtt_to_time(n, q, f, cal == null ? System.currentTimeMillis() : cal.getTimeInMillis());
	}

	// FIXME: this one isn't working properly
	public long wrapped_jtt_to_long(int n, long t) {
		long day = longToJDN(t);
		long tr[] = computeTr(day);
		if (n < 300) {// get prev sunset
			tr[1] = tr[0];
			tr[0] = computeTr(day - 1)[1];
		} else if (n > 799) {// get next sunrise
			tr[0] = tr[1];
			tr[1] = computeTr(day + 1)[0];
		}
		long offset = (tr[1] - tr[0]) * n / 600;

		return tr[0] + offset;
	}

	public long jtt_to_long(int n, int q, int f, long t) {
		return wrapped_jtt_to_long(((n * JTTHour.QUARTERS) + q) * JTTHour.PARTS + f, t);
	}

	public Calendar jtt_to_time(int n, int q, int f, long t) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(jtt_to_long(n, q, f, t));
		return cal;
	}

	public static long longToJDN(long time) {
		return (int) Math.floor(longToJD(time));
	}

	private static double longToJD(long time) {
		return time / ((double) ms_per_day) + 2440587.5;
	}

	private static long JDToLong(double jd) {
		return Math.round((jd - 2440587.5) * ms_per_day);
	}

	// it's ok to call this function often since the data is cached
	public long[] computeTr(long jdn) {
		long result[] = cache.get(jdn);
		if (result == null) {
			Calendar date = Calendar.getInstance();
			date.setTimeInMillis(JDToLong(jdn));
			Calendar rise = calculator.getOfficialSunriseCalendarForDate(date);
			Calendar set = calculator.getOfficialSunsetCalendarForDate(date);
			result = new long[] { rise.getTimeInMillis(), set.getTimeInMillis() };
			cache.put(jdn, result);
		}
		return result;
	}
}
