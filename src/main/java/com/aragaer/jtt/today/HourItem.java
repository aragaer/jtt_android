// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.today;

import com.aragaer.jtt.R;
import com.aragaer.jtt.core.Hour;
import com.aragaer.jtt.resources.RuntimeResources;
import com.aragaer.jtt.resources.StringResources;

import android.content.Context;
import android.widget.TextView;
import android.view.View;

/* Hour item in TodayList */
class HourItem extends TodayItem {
    public final int hnum;

    public HourItem(long t, int h) {
        super(t);
        hnum = h % 12;
    }

    static String[] extras;

    @Override
    public View toView(Context c, View v, int sel_p_diff) {
        if (v == null)
            v = View.inflate(c, R.layout.today_item, null);
        final StringResources sr = RuntimeResources.get(c).getStringResources();

        ((TextView) v.findViewById(R.id.glyph)).setText(Hour.Glyphs[hnum]);
        ((TextView) v.findViewById(R.id.name)).setText(sr.getHrOf(hnum));
        ((TextView) v.findViewById(R.id.extra)).setText(extras[hnum]);
        ((TextView) v.findViewById(R.id.curr)).setText(sel_p_diff == 0 ? "▶" : "");

        return v;
    }
}
