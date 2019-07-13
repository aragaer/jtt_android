// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.wadokei;

import android.content.Context;
import android.widget.TextView;

import androidx.test.filters.MediumTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@MediumTest
public class AutoresizeTextViewTest {

    @Test public void testChangeTextSize() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        float scale = context.getResources().getDisplayMetrics().density;
        TextView view = new AutoresizeTextView(context);
        view.setTextSize(42);
        view.setText("test");
        view.measure(100, 0);
        assertTrue(view.getPaint().measureText("test") <= 100);
        assertTrue(view.getTextSize() <= 42 * scale);
    }

    @Test public void testKeepSizeIfFits() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        float scale = context.getResources().getDisplayMetrics().density;
        TextView view = new AutoresizeTextView(context);
        view.setTextSize(12);
        view.setText("test");
        view.measure(100, 0);
        assertEquals(view.getTextSize(), 12*scale, 0.001);
    }
}
