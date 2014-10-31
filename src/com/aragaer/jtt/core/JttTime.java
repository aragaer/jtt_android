package com.aragaer.jtt.core;
// vim: et ts=4 sts=4 sw=4

public class JttTime {
    public static final int INTERVALS_PER_DAY = 2;
    public static final int HOURS_PER_INTERVAL = 6;
    public static final int QUARTERS_PER_HOUR = 4;
    public static final int TICKS_PER_QUARTER = 10;
    public static final int TICKS_PER_HOUR = TICKS_PER_QUARTER * QUARTERS_PER_HOUR;
    public static final int TICKS_PER_INTERVAL = TICKS_PER_HOUR * HOURS_PER_INTERVAL;
}
