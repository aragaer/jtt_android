package com.aragaer.jtt.clockwork;
// vim: set et ts=4 sts=4 sw=4

import android.content.Context;

import com.aragaer.jtt.core.DayInterval;
import com.aragaer.jtt.core.ThreeIntervals;
import com.aragaer.jtt.core.TransitionProvider;


public class AndroidAstrolabe implements Astrolabe {
    private final Context context;

    public AndroidAstrolabe(Context context) {
        this.context = context;
    }

    public DayInterval getCurrentInterval() {
        ThreeIntervals transitions = TransitionProvider.getSurroundingTransitions(context);
        return transitions.getCurrent();
    }
}
