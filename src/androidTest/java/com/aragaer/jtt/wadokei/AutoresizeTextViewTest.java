// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.wadokei;

import android.content.Context;
import android.widget.TextView;


import org.junit.*;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.*;


public class AutoresizeTextViewTest {

    private TextView view;
    private float scale;

    @Before public void setUp() {
        Context context = getInstrumentation().getTargetContext();
        view = new AutoresizeTextView(context);
        scale = context.getResources().getDisplayMetrics().density;
    }

    @Test public void testChangeTextSize() {
        view.setTextSize(42);
        view.setText("test");
        view.measure(100, 0);
        assertTrue(view.getPaint().measureText("test") <= 100);
        assertTrue(view.getTextSize() <= 42*scale);
    }

    @Test public void testKeepSizeIfFits() {
        view.setTextSize(12);
        view.setText("test");
        view.measure(100, 0);
        assertEquals(view.getTextSize(), 12*scale, 0.1);
    }
}
