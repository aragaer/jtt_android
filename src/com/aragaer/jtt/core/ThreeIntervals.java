// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.core;


public class ThreeIntervals {

    private final long[] _transitions;

    public ThreeIntervals(long[] transitions) {
	_transitions = new long[4];
	System.arraycopy(transitions, 0, _transitions, 0, 4);
    }

    public long[] getTransitions() {
	return _transitions;
    }
}
