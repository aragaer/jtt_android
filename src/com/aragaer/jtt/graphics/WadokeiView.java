// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.graphics;

import com.aragaer.jtt.core.Hour;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class WadokeiView extends View {
    private final WadokeiDraw wd;
    private Hour hour = new Hour(0);

    public WadokeiView(Context context) {
        this(context, null);
    }

    public WadokeiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        wd = new WadokeiDraw(new Paints(context, 0));
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w == oldw)
            return;
        wd.setUnit(Math.round(w / 18f));
        wd.prepare_glyphs(hour.num);
    }

    public void set_hour(final Hour new_hour) {
        if (hour.num != new_hour.num)
            wd.prepare_glyphs(new_hour.num);
        hour = new_hour;
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        wd.draw_dial(hour, canvas);
    }
}
