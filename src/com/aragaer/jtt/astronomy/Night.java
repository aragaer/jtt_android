package com.aragaer.jtt.astronomy;
// vim: et ts=4 sts=4 sw=4


public class Night implements DayInterval {
    private final long sunset;
    private final long sunrise;

    public Night(long sunset, long sunrise) {
        this.sunset = sunset;
        this.sunrise = sunrise;
    }

    public long getStart() {
        return sunset;
    }

    public long getEnd() {
        return sunrise;
    }

    public boolean isDay() {
        return false;
    }
}
