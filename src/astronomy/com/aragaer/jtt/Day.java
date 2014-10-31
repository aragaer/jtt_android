package com.aragaer.jtt;
// vim: et ts=4 sts=4 sw=4


public class Day implements DayInterval {
    private final long sunrise;
    private final long sunset;

    public Day(long sunrise, long sunset) {
        this.sunrise = sunrise;
        this.sunset = sunset;
    }

    public long getStart() {
        return sunrise;
    }

    public long getEnd() {
        return sunset;
    }

    public boolean isDay() {
        return true;
    }
}
