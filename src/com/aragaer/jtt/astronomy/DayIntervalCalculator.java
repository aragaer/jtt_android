package com.aragaer.jtt.astronomy;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.Location;


public interface DayIntervalCalculator {
    public void setLocation(Location location);
    public DayInterval getIntervalFor(long timestamp);
}
