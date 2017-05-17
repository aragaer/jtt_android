// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import com.aragaer.jtt.core.Hour;
import com.aragaer.jtt.graphics.ArrowView;
import com.aragaer.jtt.graphics.WadokeiView;
import com.aragaer.jtt.resources.RuntimeResources;
import com.aragaer.jtt.resources.StringResources;
import com.aragaer.jtt.wadokei.AutoresizeTextView;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.TypedValue;

public class ClockView extends ViewGroup implements StringResources.StringResourceChangeListener {
    private final StringResources sr;
    private final TextView text;
    private final WadokeiView wadokei;
    private final ArrowView arrow;
    private Hour hour = new Hour(0);
    private boolean vertical;

    public ClockView(Context context) {
        super(context);
        sr = RuntimeResources.get(context).getStringResources();
        sr.registerStringResourceChangeListener(this, StringResources.TYPE_HOUR_NAME);
        wadokei = new WadokeiView(context);
        arrow = new ArrowView(context);
        text = new AutoresizeTextView(context);

        TypedArray ta = context.obtainStyledAttributes(null, R.styleable.Wadokei, 0, 0);
        text.setTextColor(ta.getColor(R.styleable.Wadokei_text_stroke, 0));
        ta.recycle();

        text.setGravity(Gravity.CENTER);

        addView(wadokei);
        addView(arrow);
        addView(text);
    }

    private static final int granularity = 4;
    public void setHour(final int wrapped) {
        Hour hour = Hour.fromTickNumber(wrapped, granularity);
        if (hour.equals(this.hour))
            return;
        this.hour = hour;
        wadokei.set_hour(hour);
        text.setText(vertical ? sr.getHrOf(hour.num) : sr.getHour(hour.num));
    }

    protected void onMeasure(int wms, int hms) {
        final int w = MeasureSpec.getSize(wms);
        final int h = MeasureSpec.getSize(hms);
        vertical = h > w;
        text.setText(vertical ? sr.getHrOf(hour.num) : sr.getHour(hour.num));
        text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, vertical ? w / 20 : w / 15);
        text.measure(vertical ? w : w - h / 2, 0);
        setMeasuredDimension(w, h);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int w = r - l;
        final int h = b - t;
        if (vertical) {
            int unit = Math.round(w / 20f);
            if (changed) {
                wadokei.layout(unit, h - unit * 20, unit * 19, h - unit);
                arrow.layout(unit * 19 / 2, h - unit * 21, unit * 21 / 2, h - unit * 20);
            }
            final int tw = text.getMeasuredWidth();
            text.layout(w / 2 - tw / 2, h / 10, w / 2 + tw / 2, h / 10 + text.getMeasuredHeight());
        } else {
            int unit = Math.round(h / 20f);
            if (changed) {
                wadokei.layout(w - unit * 19, unit, w - unit, unit * 20);
                arrow.layout(w - unit * 21 / 2, 0, w - unit * 19 / 2, unit);
            }
            final int th = text.getMeasuredHeight();
            text.layout(w / 40, h / 2 - th / 2, w / 40 + text.getMeasuredWidth(), h / 2 + th / 2);
        }
    }

    public void onStringResourcesChanged(final int changes) {
        text.setText(vertical ? sr.getHrOf(hour.num) : sr.getHour(hour.num));
    }
}
