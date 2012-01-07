package com.aragaer.jtt;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.util.Log;

public class JTT {
    private final SolarObserver calculator;
    public long rate; // number of millis per 1% of hour
    public Date nextHour; // when next hour will begin

    public JTT(float latitude, float longitude, TimeZone tz) {
        this.calculator = new SolarObserver(latitude, longitude, tz);

        Calendar now = Calendar.getInstance();
        long[] c = new long[2];
        getTrasitions(now, c);

        rate = Math.round(c[1]/600.0);

        now.add(Calendar.SECOND, (int) Math.round(rate/1000.0*Math.ceil(6.0*c[0]/c[1])));
        nextHour = now.getTime();
    }

    /* returns false if day, true if night
     * out contains ms from last transition and ms between transitions
     */
    private Boolean getTrasitions(Calendar cal, long[] out) {
        Boolean isNight = true;
        final Calendar sunrise = calculator.sunrise(cal);
        final Calendar sunset = calculator.sunset(cal);
        long a, b, c;

        b = cal.getTimeInMillis();
        if (cal.before(sunrise)) {
            cal.add(Calendar.DAY_OF_YEAR, -1);
            a = calculator.sunset(cal).getTimeInMillis();
            c = sunrise.getTimeInMillis();
        } else if (cal.after(sunset)) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
            c = calculator.sunrise(cal).getTimeInMillis();
            a = sunset.getTimeInMillis();
        } else {
            isNight = false;
            a = sunrise.getTimeInMillis();
            c = sunset.getTimeInMillis();
        }
        out[0] = b - a;
        out[1] = c - a;

        return isNight;
    }

    public JTTHour time_to_jtt(Date time) {
        Calendar cal = Calendar.getInstance();
        long[] c = new long[2];
        cal.setTime(time);
        Boolean isNight = getTrasitions(cal, c);

        return new JTTHour(6.0f * c[0] / c[1] + (isNight ? 0 : 6));
    }

    public Date jtt_to_time(JTTHour hour, Date date) {
        Calendar cal = (Calendar) date.clone();

        return cal.getTime();
    }
}

class SolarObserver {
    final private float latitude;
    final private float longitude;
    final private TimeZone timeZone;
    final private Boolean useCivilZenith = false;
    final private double zenithCivil = 96;
    final private double zenithOfficial = 90.8333;

    public SolarObserver(float latitude, float longitude, TimeZone timezone) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeZone = timezone;
    }

    public Calendar sunrise(Calendar date) {
        return getLocalTimeAsCalendar(computeSolarEventTime(date, true), date);
    }

    public Calendar sunset(Calendar date) {
        return getLocalTimeAsCalendar(computeSolarEventTime(date, false), date);
    }

    // using this: http://williams.best.vwh.net/sunrise_sunset_algorithm.htm
    private double computeSolarEventTime(Calendar date, boolean isSunrise) {
        date.setTimeZone(this.timeZone);

        double lngHour = longitude / 15;
        double t = date.get(Calendar.DAY_OF_YEAR)
                + ((isSunrise ? 6 : 18) - lngHour) / 24;

        double M = (0.9856 * t) - 3.289;

        double L = M + (1.916 * Math.sin(Math.toRadians(M)))
                + (0.020 * Math.sin(Math.toRadians(2 * M))) + 282.634;
        if (L < 0)
            L += 360.0;
        else if (L > 360.0)
            L -= 360.0;

        double RA = Math.toDegrees(Math.atan(0.91764 * Math.tan(Math
                .toRadians(L))));
        RA = RA / 15 + (Math.floor(L / 90) - Math.floor(RA / 90)) * 6;

        double sinDec = 0.39782 * Math.sin(Math.toRadians(L));
        double cosDec = Math.cos(Math.asin(sinDec));

        double zenith = Math.toRadians(useCivilZenith ? zenithCivil
                : zenithOfficial);
        double radlat = Math.toRadians(latitude);
        double cosH = (Math.cos(zenith) - (sinDec * Math.sin(radlat)))
                / (cosDec * Math.cos(radlat));
        if (Math.abs(cosH) > 1)
            return -1;

        double H = Math.toDegrees(Math.acos(cosH)) / 15;
        if (isSunrise)
            H = 24 - H;

        double T = H + RA - (0.06571 * t) - 6.622;
        double UT = T - lngHour;
        double LocalT = UT
                + (date.get(Calendar.ZONE_OFFSET) + date
                        .get(Calendar.DST_OFFSET)) / 3600000.0;
        if (LocalT < 0)
            LocalT += 24;
        else if (LocalT >= 24)
            LocalT -= 24;

        return LocalT;
    }

    private Calendar getLocalTimeAsCalendar(double localTime, Calendar date) {
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
}
