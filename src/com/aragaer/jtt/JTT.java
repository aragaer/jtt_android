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

//	private final static int HOUR_OF_COCK = wrap_jtt(0, 0, 0); // 0
	private final static int HOUR_OF_RAT = wrap_jtt(3, 0, 0);
	private final static int HOUR_OF_HARE = wrap_jtt(6, 0, 0);

	public JTTHour time_to_jtt(Calendar c, JTTHour reuse) {
		return time_to_jtt(c == null ? System.currentTimeMillis() : c.getTimeInMillis(), reuse);
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
		return (HOUR_PARTS / 2 + (int) (TOTAL_PARTS * (now - tr0) / (tr1 - tr0))) % TOTAL_PARTS;
	}

	public static int wrap_jtt(int hour, int quarter, int part) {
		return (hour * JTTHour.QUARTERS + quarter) * JTTHour.PARTS + part;
	}

	/* wrapped 0 means h 0 quarter 2 part 0 actually */
	public static JTTHour unwrap_jtt(int wrapped, JTTHour reuse) {
		if (reuse == null)
			reuse = new JTTHour(0);
		wrapped += HOUR_PARTS / 2;
		final int h = (wrapped / HOUR_PARTS) % 12;
		wrapped %= HOUR_PARTS;
		reuse.setTo(h, wrapped / JTTHour.PARTS, wrapped % JTTHour.PARTS);
		return reuse;
	}

	public JTTHour time_to_jtt(long time, JTTHour reuse) {
		return unwrap_jtt(time_to_jtt_wrapped(time), reuse);
	}

	// helper function,
	// accepts two transition times and "current" wrapped jtt
	// returns timestamp from [tr0; tr1) appropriate for jtt
	// assumes jtt falls into interval
	public static long wrapped_tr_to_time(long tr0, long tr1, int wrapped) {
		wrapped -= HOUR_PARTS / 2;
		return tr0 + (tr1 - tr0) * wrapped / TOTAL_PARTS;
	}

	public Calendar jtt_to_time(JTTHour hour, Calendar cal) {
		return jtt_to_time(hour.num, hour.quarter, hour.quarter_parts, cal);
	}

	public Calendar jtt_to_time(int n, int q, int f, Calendar cal) {
		return jtt_to_time(n, q, f, cal == null ? System.currentTimeMillis() : cal.getTimeInMillis());
	}

	public long wrapped_jtt_to_long(int n, long t) {
		long day = longToJDN(t);
		long rat2 = rat(day + 1);
		long tr0, tr1;

		while (t >= rat2) {
			day++;
			rat2 = rat(day + 1);
		}

		// t is now fixed between two midnights
		if (n < HOUR_OF_RAT /* && n >= HOUR_OF_COCK */) { // early night
			tr0 = sunset(day);
			tr1 = sunrise(day + 1);
		} else if (n >= HOUR_OF_HARE) { // day
			tr0 = sunrise(day);
			tr1 = sunset(day);
			n -= TOTAL_PARTS;
		} else { // late night
			tr0 = sunset(day - 1);
			tr1 = sunrise(day);
		}

		return wrapped_tr_to_time(tr0, tr1, n);
	}

	// helper function - return hour of rat (midnight) that starts given jdn
	public long rat(long day) {
		return (sunrise(day) + sunset(day - 1)) / 2;
	}

	// this one is just a wrapper
	public long hour_start(int n, long t) {
		return jtt_to_long(n, 0, 0, t);
	}

	// this one is a bit more complex..
	public long hour_end(int n, long t) {
		n++;
		n %= 12;
		if (n == 3) // hour of rat
			return rat(longToJDN(t) + 1);
		return jtt_to_long(n, 0, 0, t);
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

	// wrappers
	public long sunrise(long jdn) {
		return computeTr(jdn)[0];
	}

	public long sunset(long jdn) {
		return computeTr(jdn)[1];
	}
}
