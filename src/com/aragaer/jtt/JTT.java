package com.aragaer.jtt;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class JTT {
	private double latitude, longitude;

	public JTT(float lat, float lon) {
		latitude = lat;
		longitude = lon;
	}

	public void move(float lat, float lon) {
		latitude = lat;
		longitude = lon;
	}

	static final long ms_per_minute = TimeUnit.SECONDS.toMillis(60);
	static final long ms_per_hour = TimeUnit.SECONDS.toMillis(60 * 60);
	static final long ms_per_day = TimeUnit.SECONDS.toMillis(60 * 60 * 24);

	public JTTHour time_to_jtt(Date d) {
		return time_to_jtt(d == null ? System.currentTimeMillis() : d.getTime());
	}

	public long time_to_jtt_wrapped(long time) {
		final long day = longToJDN(time);
		final long tr[] = computeTr(day);
		int dayAdd = 0;
		if (time < tr[0]) { // before sunrise. Get prev sunset
			tr[1] = tr[0];
			tr[0] = computeTr(day - 1)[1];
		} else if (time > tr[1]) { // after sunset. Get next sunrise
			tr[0] = tr[1];
			tr[1] = computeTr(day + 1)[0];
		} else {
			dayAdd = 600;
		}
		return 600 * (time - tr[0]) / (tr[1] - tr[0]) + dayAdd;
	}

	public JTTHour time_to_jtt(long time) {
		long h = time_to_jtt_wrapped(time);
		return new JTTHour((int) (h / 100), (int) h % 100);
	}

	public Date jtt_to_time(JTTHour hour, Date date) {
		return jtt_to_time(hour.num, hour.fraction, date);
	}

	public Date jtt_to_time(int n, int f, Date date) {
		return jtt_to_time(n, f, date == null ? System.currentTimeMillis() : date.getTime());
	}

	public long jtt_to_long(int n, int f, long t) {
		long d = longToJDN(t);
		long tr[] = computeTr(d);
		if (n < 3) {// get prev sunset
			tr[1] = tr[0];
			tr[0] = computeTr(d - 1)[1];
		} else if (n > 8) {// get next sunrise
			tr[0] = tr[1];
			tr[1] = computeTr(d + 1)[0];
		}
		long offset = (tr[1] - tr[0]) * (n * 100 + f) / 600;

		return tr[0] + offset;
	}

	public Date jtt_to_time(int n, int f, long t) {
		return new Date(jtt_to_long(n, f, t));
	}

	private final static double sin(double g) {
		return Math.sin(Math.toRadians(g));
	}

	private final static double cos(double g) {
		return Math.cos(Math.toRadians(g));
	}

	private final static double asin(double a) {
		return Math.toDegrees(Math.asin(a));
	}

	private final static double acos(double a) {
		return Math.toDegrees(Math.acos(a));
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

	// http://en.wikipedia.org/wiki/Sunrise_equation#Complete_calculation_on_Earth
	public long[] computeTr(long jdn) {
		final double n_star = jdn - 2451545.0009 + longitude / 360.0;
		final double n = Math.floor(n_star + 0.5);
		final double j_star = 2451545.0009 - longitude / 360.0 + n;
		final double M = (357.5291 + 0.98560028 * (j_star - 2451545)) % 360;
		final double C = 1.9148 * sin(M) + 0.02 * sin(2 * M) + 0.0003
				* sin(3 * M);
		final double lambda = (M + 102.9372 + C + 180) % 360;
		final double j_transit = j_star + 0.0053 * sin(M) - 0.0069
				* sin(2 * lambda);
		final double sigma = asin(sin(lambda) * sin(23.44));

		final double omega0 = acos((sin(-0.83) - sin(latitude) * sin(sigma))
				/ cos(latitude) * cos(sigma));
		final double j_set = 2451545.0009 + (omega0 - longitude) / 360.0 + n
				+ 0.0053 * sin(M) - 0.0069 * sin(2 * lambda);
		final double j_rise = j_transit - (j_set - j_transit);

		return new long[] { JDToLong(j_rise), JDToLong(j_set) };
	}
}
