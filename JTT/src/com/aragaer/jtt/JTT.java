package com.aragaer.jtt;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import android.util.Log;

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

    /*
     * Helper function. Returns false if day, true if night. Out contains ms
     * from last transition and ms between transitions
     */
    private boolean getTransitions(long b, long[] out) {
        boolean isNight = true;
        int d = longToJDN(b);
        // Log.d("compute", "jdn = "+d);
        long tr[] = computeTr(d);
        // Log.d("transitions", "sunrise "+tr[0]+", time "+b+", sunset "+tr[1]);
        if (b < tr[0]) { // before sunrise. Get prev sunset
            tr[1] = tr[0];
            tr[0] = computeTr(d - 1)[1];
        } else if (b > tr[1]) { // after sunset. Get next sunrise
            tr[0] = tr[1];
            tr[1] = computeTr(d + 1)[0];
        } else {
            isNight = false;
        }
        // Log.d("transitions", "= "+a+", "+b+", "+c);
        // Log.d("transitions", "a = "+(new Date(a)).toLocaleString());
        // Log.d("transitions", "b = "+(new Date(b)).toLocaleString());
        // Log.d("transitions", "c = "+(new Date(c)).toLocaleString());

        out[0] = b - tr[0];
        out[1] = tr[1] - tr[0];
        Log.d("transitions", (isNight ? "night " : "day ") + out[0] + ":" + out[1]);

        return isNight;
    }

    private static JTTHour transitionsToHour(long c[], boolean isNight) {
        final long h = (600 * c[0] / c[1] + (isNight ? 0 : 600)) % 1200;
        return new JTTHour((int) h / 100, (int) h % 100);
    }

    public JTTHour time_to_jtt(Date d) {
        long time = d == null ? System.currentTimeMillis() : d.getTime();
        long[] c = new long[2];
        return transitionsToHour(c, getTransitions(time, c));
    }

    public Date jtt_to_time(JTTHour hour, Date date) {
        long t = date == null ? System.currentTimeMillis() : date.getTime();
        int d = longToJDN(t);
        long tr[] = computeTr(d);
        if (hour.num < 3) {// get prev sunset
            tr[1] = tr[0];
            tr[0] = computeTr(d - 1)[1];
        } else if (hour.num > 8) {// get next sunrise
            tr[0] = tr[1];
            tr[1] = computeTr(d + 1)[0];
        }
        long offset = (tr[1] - tr[0]) * (hour.num * 100 + hour.fraction) / 600;

        return new Date(tr[0] + offset);
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

    public static int longToJDN(long time) {
        return (int) Math.floor(longToJD(time));
    }

    private static double longToJD(long time) {
        return time / ((double) ms_per_day) + 2440587.5;
    }

    private static long JDToLong(double jd) {
        return Math.round((jd - 2440587.5) * ms_per_day);
    }

    // http://en.wikipedia.org/wiki/Sunrise_equation#Complete_calculation_on_Earth
    public long[] computeTr(int jdn) {
        final double n_star = jdn - 2451545.0009 + longitude / 360.0;
        final double n = Math.floor(n_star + 0.5);
        // Log.d("compute", "it's julian cycle "+((int) n));
        final double j_star = 2451545.0009 - longitude / 360.0 + n;
        final double M = (357.5291 + 0.98560028 * (j_star - 2451545)) % 360;
        // Log.d("compute", "solar mean anomaly is "+M);
        final double C = 1.9148 * sin(M) + 0.02 * sin(2 * M) + 0.0003
                * sin(3 * M);
        final double lambda = (M + 102.9372 + C + 180) % 360;
        final double j_transit = j_star + 0.0053 * sin(M) - 0.0069
                * sin(2 * lambda);
        // Log.d("compute", "noon is at "+(new Date(JDToLong(j_transit))).toLocaleString());
        final double sigma = asin(sin(lambda) * sin(23.44));
        // Log.d("compute", "declination is "+sigma);

        final double omega0 = acos((sin(-0.83) - sin(latitude) * sin(sigma))
                / cos(latitude) * cos(sigma));
        final double j_set = 2451545.0009 + (omega0 - longitude) / 360.0 + n
                + 0.0053 * sin(M) - 0.0069 * sin(2 * lambda);
        final double j_rise = j_transit - (j_set - j_transit);
        // Log.d("compute", "julian rise = "+j_rise+", transit ="+j_transit+", set = "+j_set);

        return new long[] { JDToLong(j_rise), JDToLong(j_set) };
    }
}
