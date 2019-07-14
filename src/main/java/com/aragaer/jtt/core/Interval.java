// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;

import java.io.Serializable;


public class Interval implements Serializable {

    public final long start, end;
    public final boolean is_day;

    public Interval(long start, long end, boolean is_day) {
        if (start > end)
            throw new IllegalArgumentException();
        this.start = start;
        this.end = end;
        this.is_day = is_day;
    }

    public long getLength() {
        return end - start;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Interval other = (Interval) o;
        return is_day == other.is_day && start == other.start && end == other.end;
    }

    @Override public int hashCode() {
        int result = 0;
        result = 31*result + (int) (start ^ (start >>> 32));
        result = 31*result + (int) (end ^ (end >>> 32));
        return is_day ? result : ~result;
    }
}
