// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.ui.android;

import android.text.InputFilter;
import android.text.Spanned;


public class InputFilterMinMax implements InputFilter {
    private float min, max;

    public InputFilterMinMax(float min, float max) {
        this.min = min;
        this.max = max;
    }

    @Override public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String checkedText = dest.subSequence(0, dstart).toString() +
            source.subSequence(start, end) +
            dest.subSequence(dend,dest.length()).toString();
        if (checkedText.equals("-"))
            return null;
        try {
            float input = Float.parseFloat(checkedText);
            if (isInRange(min, max, input))
                return null;
        } catch (NumberFormatException nfe) { }
        return "";
    }

    private static boolean isInRange(float a, float b, float c) {
        return c >= a && c <= b; // assume b >= a
    }
}
