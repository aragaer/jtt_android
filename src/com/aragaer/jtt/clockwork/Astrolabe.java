package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.core.DayInterval;


public interface Astrolabe {
    public DayInterval getCurrentInterval();
    public void updateLocation();
}
