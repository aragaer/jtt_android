package com.aragaer.jtt;

import java.util.Calendar;
import java.util.Date;
//import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

public class JTT {
    private final static int MSG = 1;
    public Date start, end;
    private TickHandler tick;
    public JTTHour now;

    private SolarObserver calculator;
    private long rate; // number of millis per 1% of hour

    public JTT(float latitude, float longitude/*, TimeZone tz*/) {
        calculator = new SolarObserver(latitude, longitude/*, tz*/);
        now = time_to_jtt(null);
    }

    public void move(float latitude, float longitude/*, TimeZone tz*/) {
        calculator = new SolarObserver(latitude, longitude/*, tz*/);
        now = time_to_jtt(null);
    }

    /*
     * Helper function. Returns false if day, true if night. Out contains ms
     * from last transition and ms between transitions
     */
    private boolean getTransitions(Calendar cal, long[] out) {
        boolean isNight = true;
        long a, b, c;
        b = cal.getTimeInMillis() % SolarObserver.ms_per_day;
        int d = cal.get(Calendar.DAY_OF_YEAR);
        final long sunrise = calculator.sunrise(d);
        final long sunset = calculator.sunset(d);
        Log.d("transitions", "sunrise "+sunrise+", time "+b+", sunset "+sunset);
        if (b < sunrise) {
            a = calculator.sunset((d + 364) % 365);
            c = sunrise + SolarObserver.ms_per_day;
        } else if (b > sunset) {
            c = calculator.sunrise((d + 1) % 365);
            a = sunset - SolarObserver.ms_per_day;
        } else {
            isNight = false;
            a = sunrise;
            c = sunset;
        }
        Log.d("transitions", "= "+a+", "+b+", "+c);
        
        out[0] = b - a;
        out[1] = c - a;
        Log.d("transitions", (isNight ? "night ": "day ")+out[0]+":"+out[1]);

        return isNight;
    }

    private static JTTHour transitionsToHour(long c[], boolean isNight) {
        final long h = (600 * c[0] / c[1] + (isNight ? 0 : 600)) % 1200;
        return new JTTHour((int) h / 100, (int) h % 100);
    }

    public JTTHour time_to_jtt(Date time) {
        Calendar cal = Calendar.getInstance();
        long[] c = new long[2];
        if (time != null)
            cal.setTime(time);
        return transitionsToHour(c, getTransitions(cal, c));
    }

    public Date jtt_to_time(JTTHour hour, Date date) {
        Calendar cal = (Calendar) date.clone();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long[] c = new long[2];
        if (hour.num < 3) // move to next midnight
            cal.add(Calendar.DATE, 1);
        else if (hour.num < 9) // move to midday
            cal.set(Calendar.HOUR_OF_DAY, 12);
        getTransitions(cal, c);

        long offset = (c[1] * (hour.num % 6)) / 6 - c[0];
        final int ms = (int) (offset % 1000);
        offset /= 1000;
        final int s = (int) (offset % 60);
        cal.add(Calendar.MILLISECOND, ms);
        cal.add(Calendar.SECOND, s);

        return cal.getTime();
    }

    public void registerTicker(TickHandler tickTask) {
        tick = tickTask;
    }

    /* updates rate, recalculates now, returns delay to next tick */
    private long resync_tick() {
        Calendar cal = Calendar.getInstance();
        long[] c = new long[2];
        now = transitionsToHour(c, getTransitions(cal, c));
        rate = Math.round(c[1] / 600.0);
        final long start_ms = cal.getTimeInMillis() - now.fraction * rate;
        start = new Date(start_ms);
        end = new Date(start_ms + rate * 100);
        return rate - c[0] % rate;
    }

    public synchronized final void start_ticking() {
        long delay = resync_tick();
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG), delay);
        if (delay > 1000) // arbitrary threshold for doing a callback
                          // immediately
            tick.handle(now);
    }

    public final void stop_ticking() {
        mHandler.removeMessages(MSG);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            synchronized (JTT.this) {
                long lastTickStart = SystemClock.elapsedRealtime();
                now.fraction++;

                if ((now.fraction % 10) == 0) // do resync now
                    lastTickStart += resync_tick();
                tick.handle(now);

                // take into account user's onTick taking time to execute
                long delay = lastTickStart - SystemClock.elapsedRealtime();
                // special case: user's onTick took more than interval to
                // complete, skip to next interval
                while (delay < 0)
                    delay += rate;
                sendMessageDelayed(obtainMessage(MSG), delay);
            }
        }
    };

    public static abstract class TickHandler {
        abstract public void handle(JTTHour h);
    }
}

class SolarObserver {
    final private float latitude;
    final private float longitude;
//    final private TimeZone timeZone;
    final private static double zenithOfficial = 90.8333;
    final private static double zenith = Math.toRadians(zenithOfficial);

/*
    private static final int longToDay(long l) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(l);
        return cal.get(Calendar.DAY_OF_YEAR);
    }
    */

    public SolarObserver(float latitude, float longitude/*, TimeZone timezone*/) {
        this.latitude = latitude;
        this.longitude = longitude;
//        this.timeZone = timezone;
    }
/*
    public Calendar sunrise(Calendar date) {
        return getLocalTimeAsCalendar(computeSolarEventTime(date, true), date);
    }

    public Calendar sunset(Calendar date) {
        return getLocalTimeAsCalendar(computeSolarEventTime(date, false), date);
    }
    */
/*
    public long sunrise(long date) {
        date += timeZone.getRawOffset();
        return getLocalTimeAsLong(computeSolarEventTime2(longToDay(date), true), date);
    }

    public long sunset(long date) {
        date += timeZone.getRawOffset();
        return getLocalTimeAsLong(computeSolarEventTime2(longToDay(date), false), date);
    }
*/
    public long sunrise(int day_of_year) {
        return (long) (computeSolarEventTime2(day_of_year, true) * ms_per_hour);
    }

    public long sunset(int day_of_year) {
        return (long) (computeSolarEventTime2(day_of_year, false) * ms_per_hour);
    }

    // using this: http://williams.best.vwh.net/sunrise_sunset_algorithm.htm
/*
    private double computeSolarEventTime(Calendar date, boolean isSunrise) {
        final double LocalT = computeSolarEventTime2(date.get(Calendar.DAY_OF_YEAR), isSunrise)
                + timeZone.getRawOffset() / ms_per_hour;
        return LocalT % 24;
    }
*/
    private double computeSolarEventTime2(int day, boolean isSunrise) {
        final double lngHour = longitude / 15;
        final double t = day + ((isSunrise ? 6 : 18) - lngHour) / 24;

        final double M = (0.9856 * t) - 3.289;

        final double L = (M + (1.916 * Math.sin(Math.toRadians(M)))
                + (0.020 * Math.sin(Math.toRadians(2 * M))) + 282.634) % 360.0;

        double RA = Math.toDegrees(Math.atan(0.91764 * Math.tan(Math
                .toRadians(L))));
        RA = RA / 15 + (Math.floor(L / 90) - Math.floor(RA / 90)) * 6;

        final double sinDec = 0.39782 * Math.sin(Math.toRadians(L));
        final double cosDec = Math.cos(Math.asin(sinDec));

        final double radlat = Math.toRadians(latitude);
        final double cosH = (Math.cos(zenith) - (sinDec * Math.sin(radlat)))
                / (cosDec * Math.cos(radlat));
        if (Math.abs(cosH) > 1)
            return -1;

        final double H = Math.toDegrees(Math.acos(cosH)) / 15;

        final double T = (isSunrise ? 24 - H : H) + RA - (0.06571 * t) - 6.622;
        final double LocalT = T - lngHour;
        return LocalT % 24;
    }
/*
    private static Calendar getLocalTimeAsCalendar(double localTime,
            Calendar date) {
        if (localTime < 0)
            return null;

        Calendar resultTime = (Calendar) date.clone();
        int hour = (int) localTime;
        localTime = (localTime - hour) * 60;

        int minutes = (int) localTime;
        if (minutes == 60) {
            minutes = 0;
            hour += 1;
        }

        localTime = (localTime - minutes) * 60;
        int seconds = (int) localTime;
        if (seconds == 60) {
            seconds = 0;
            minutes += 1;
        }

        resultTime.set(Calendar.HOUR_OF_DAY, hour);
        resultTime.set(Calendar.MINUTE, minutes);
        resultTime.set(Calendar.SECOND, seconds);

        return resultTime;
    }
*/
    public static final long ms_per_minute = TimeUnit.SECONDS.toMillis(60);
    public static final long ms_per_hour = TimeUnit.SECONDS.toMillis(60 * 60); 
    public static final long ms_per_day = TimeUnit.SECONDS.toMillis(60 * 60 * 24);
    /*
    private static long getLocalTimeAsLong(double time,
            long date) {
        if (time < 0)
            return 0;
        return date - (date % ms_per_day) + (long) (time * ms_per_hour);
    }
    */
}
