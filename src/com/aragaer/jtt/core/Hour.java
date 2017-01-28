// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;

public class Hour {
    public static final int HOURS_PER_INTERVAL = 6,
        QUARTERS = 4,
        TICKS_PER_QUARTER = 10,
        TICKS_PER_HOUR = QUARTERS * TICKS_PER_QUARTER,
        TICKS_PER_INTERVAL = HOURS_PER_INTERVAL * TICKS_PER_HOUR,
        TICKS_PER_DAY = TICKS_PER_INTERVAL * 2;
    public static final String Glyphs[] = "酉戌亥子丑寅卯辰巳午未申".split("(?!^)");

    public int num, // 0 to 11, where 0 is hour of Cock and 11 is hour of Monkey
        quarter, // 0 to 3
        tick, // 0 of PARTS
        wrapped; // wrapped into single integer

    public Hour(int num) {
        this(num, 0, 0);
    }

    public Hour(int n, int q, int f) {
        num = n;
        quarter = q;
        tick = f;
        wrapped = (n * QUARTERS + quarter - 2) * TICKS_PER_QUARTER + tick;
        wrapped = (wrapped + TICKS_PER_DAY) % TICKS_PER_DAY;
    }

    public static Hour fromInterval(Interval interval, final long now) {
        double fractionOfIntervalPassed = (1. * now - interval.start) / interval.getLength();
        int tickNumber = (int) (TICKS_PER_INTERVAL * fractionOfIntervalPassed);
        if (interval.is_day)
            tickNumber += TICKS_PER_INTERVAL;
        return fromTickNumber(tickNumber);
    }

    public static Hour fromTickNumber(int f) {
        final int q = f / TICKS_PER_QUARTER + 2;
        final int n = q / QUARTERS;
        return new Hour(n % Glyphs.length, q % QUARTERS, f % TICKS_PER_QUARTER);
    }

    /* 0, 6 -> 5; 1-5, 7-11 -> 0-4 */
    public static final int lowerBoundary(final int hour) {
        return (hour + HOURS_PER_INTERVAL - 1) % HOURS_PER_INTERVAL;
    }

    /* 0-11 -> 0-5 */
    public static final int upperBoundary(final int hour) {
        return hour % HOURS_PER_INTERVAL;
    }


    /* given start and end of time interval return hour boundary for given position */
    public static long getHourBoundary(final long start, final long end, final int pos) {
        final long half_hlen = (end - start) / HOURS_PER_INTERVAL / 2;
        return start + half_hlen * pos * 2 + half_hlen;
    }

    public Hour truncate(int granularity) {
        int new_wrapped = wrapped - wrapped % granularity;
        return Hour.fromTickNumber(new_wrapped);
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Hour other = (Hour) o;
        return wrapped == other.wrapped;
    }

    @Override public int hashCode() {
        return wrapped;
    }
}
