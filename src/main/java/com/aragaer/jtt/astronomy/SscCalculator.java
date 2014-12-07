package com.aragaer.jtt.astronomy;
// vim: et ts=4 sts=4 sw=4

import java.util.Calendar;
import java.util.TimeZone;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;

import com.aragaer.jtt.location.Location;


public class SscCalculator implements DayIntervalCalculator {
    private SunriseSunsetCalculator calculator;

    public void setLocation(Location location) {
        calculator = new SunriseSunsetCalculator(translateLocation(location), TimeZone.getDefault());
    }

    public DayInterval getIntervalFor(long timestamp) {
        return new IntervalCalculationRunner(calculator, timestamp).calculate();
    }

    private static class IntervalCalculationRunner {
        private final SunriseSunsetCalculator calculator;
        private final Calendar calendar;
        private final long timestamp;

        IntervalCalculationRunner(SunriseSunsetCalculator calculator, long timestamp) {
            this.calculator = calculator;
            this.timestamp = timestamp;
            calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
        }

        private long computeSunrise() {
            return calculator.getOfficialSunriseCalendarForDate(calendar).getTimeInMillis();
        }

        private long computeSunset() {
            return calculator.getOfficialSunsetCalendarForDate(calendar).getTimeInMillis();
        }

        private DayInterval previousNight(long sunrise) {
            calendar.add(Calendar.DATE, -1);
            return DayInterval.Night(computeSunset(), sunrise);
        }

        private DayInterval nextNight(long sunset) {
            calendar.add(Calendar.DATE, 1);
            return DayInterval.Night(sunset, computeSunrise());
        }

        DayInterval calculate() {
            long sunrise = computeSunrise();
            if (timestamp < sunrise)
                return previousNight(sunrise);

            long sunset = computeSunset();
            if (timestamp >= sunset)
                return nextNight(sunset);

            return DayInterval.Day(sunrise, sunset);
        }
    }

    private static com.luckycatlabs.sunrisesunset.dto.Location translateLocation(Location location) {
        return new com.luckycatlabs.sunrisesunset.dto.Location(location.getLatitude(), location.getLongitude());
    }
}
