package com.aragaer.jtt;
// vim: et ts=4 sts=4 sw=4

public interface DayIntervalCalculator {
    public void setLocation(Location location);
    public DayInterval getIntervalFor(long timestamp);
}
