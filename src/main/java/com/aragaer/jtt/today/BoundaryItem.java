// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.today;

import com.aragaer.jtt.R;
import com.aragaer.jtt.resources.RuntimeResources;
import com.aragaer.jtt.resources.StringResources;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;

/* Hour boundary item in TodayList */
class BoundaryItem extends TodayItem {
    public BoundaryItem(long t) {
        super(t);
    }

    @Override
    public View toView(Context c, View v, int sel_p_diff) {
        if (v == null)
            v = View.inflate(c, R.layout.today_boundary_item, null);
        final StringResources sr = RuntimeResources.get(c).getStringResources();
        ((TextView) v.findViewById(R.id.time)).setText(sr.format_time(time));
        int level;
        switch (sel_p_diff) {
        case 1: level = 1; break;
        case -1: level = 2; break;
        default: level = 0; break;
        }
        ((ImageView) v.findViewById(R.id.border)).setImageLevel(level);
        return v;
    }
}
