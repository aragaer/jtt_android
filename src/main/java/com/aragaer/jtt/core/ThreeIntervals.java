// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;

import java.io.Serializable;
import java.util.Arrays;


public class ThreeIntervals implements Serializable {
    private static final long serialVersionUID = 1;
    private final long[] _transitions = new long[4];
    private final boolean _is_day;

    public ThreeIntervals(long[] transitions, boolean is_day) {
        System.arraycopy(transitions, 0, _transitions, 0, 4);
        _is_day = is_day;
    }

    public ThreeIntervals(Interval first, Interval second, Interval third) {
        if (first.end != second.start || second.end != third.start
            || first.is_day == second.is_day || second.is_day == third.is_day)
            throw new IllegalArgumentException();
        _transitions[0] = first.start;
        _transitions[1] = second.start;
        _transitions[2] = second.end;
        _transitions[3] = third.end;
        _is_day = second.is_day;
    }

    public long[] getTransitions() {
        return _transitions;
    }

    public boolean isDay() {
        return _is_day;
    }

    public boolean surrounds(long timestamp) {
        return timestamp >= _transitions[1] && timestamp < _transitions[2];
    }

    public Interval getMiddleInterval() {
        return new Interval(_transitions[1], _transitions[2], _is_day);
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ThreeIntervals other = (ThreeIntervals) o;
        return _is_day == other._is_day && Arrays.equals(_transitions, other._transitions);
    }

    @Override public int hashCode() {
        int result = 0;
        for (long transition : _transitions)
            result = 31*result + (int) (transition ^ (transition >>> 32));
        return _is_day ? result : ~result;
    }
}
