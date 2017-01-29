// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.wadokei;

import android.test.AndroidTestCase;
import android.widget.TextView;


public class AutoresizeTextViewTest extends AndroidTestCase {

    public void testChangeTextSize() {
        float scale = getContext().getResources().getDisplayMetrics().density;
        TextView view = new AutoresizeTextView(getContext());
        view.setTextSize(42);
        view.setText("test");
        view.measure(100, 0);
        assertTrue(view.getPaint().measureText("test") <= 100);
        assertTrue(view.getTextSize() <= 42*scale);
    }

    public void testKeepSizeIfFits() {
        float scale = getContext().getResources().getDisplayMetrics().density;
        TextView view = new AutoresizeTextView(getContext());
        view.setTextSize(12);
        view.setText("test");
        view.measure(100, 0);
        assertEquals(view.getTextSize(), 12*scale);
    }
}
