// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.today;

import com.aragaer.jtt.R;
import com.aragaer.jtt.core.Hour;
import com.aragaer.jtt.core.ThreeIntervals;
import com.aragaer.jtt.resources.RuntimeResources;
import com.aragaer.jtt.resources.StringResources;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import java.lang.System;
import java.util.Arrays;

public class TodayAdapter extends ArrayAdapter<TodayItem> implements
                                                              StringResources.StringResourceChangeListener {
    private ThreeIntervals _intervals;
    private int selected;

    public TodayAdapter(Context c, int layout_id) {
        super(c, layout_id);
        RuntimeResources.get(c).getStringResources()
            .registerStringResourceChangeListener(this,
                                                  StringResources.TYPE_HOUR_NAME | StringResources.TYPE_TIME_FORMAT);
        HourItem.extras = new String[] { c.getString(R.string.sunset), "", "",
                                         c.getString(R.string.midnight), "", "",
                                         c.getString(R.string.sunrise), "", "",
                                         c.getString(R.string.noon), "", "" };
        setNotifyOnChange(false);
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        return getItem(position).toView(parent.getContext(), v, selected - position);
    }

    /* takes a sublist of hours
     * creates a list to display by adding day names
     */
    private void buildItems() {
        final long[] transitions = _intervals.getTransitions();
        clear();

        int h_add = _intervals.isDay() ? 0 : Hour.HOURS_PER_INTERVAL;

        /* start with first transition */
        add(new HourItem(transitions[0], h_add));
        for (int i = 1; i < transitions.length; i++) {
            final long start = transitions[i - 1];
            final long diff = transitions[i] - start;
            for (int j = 1; j <= Hour.HOURS_PER_INTERVAL; j++) {
                add(new BoundaryItem(start + (j * 2 - 1) * diff / Hour.HOURS_PER_INTERVAL / 2));
                add(new HourItem(start + j * diff / Hour.HOURS_PER_INTERVAL, h_add + j));
            }
            h_add = Hour.HOURS_PER_INTERVAL - h_add;
        }
    }

    public void tick(ThreeIntervals intervals) {
        if (!intervals.equals(_intervals)) {
            _intervals = intervals;
            buildItems();
        }

        // check that items are built
        // expect 37 items
        if (getCount() < Hour.HOURS_PER_INTERVAL * 3 * 2 - 1)
            // transitions are set but items aren't built
            // this means we're currently in the build process
            return;

        // odd items - boundaries
        selected = 0;
        long now = System.currentTimeMillis();
        while (getItem(selected + 1).time < now)
            selected += 2;

        notifyDataSetChanged();
    }

    @Override
    public boolean isEnabled(int pos) {
        return false;
    }

    public void onStringResourcesChanged(int changes) {
        notifyDataSetChanged();
    }

    @Override
    public int getViewTypeCount() {
        return 2; // hours and borders
    }

    @Override
    public int getItemViewType(int position) {
        return position % 2; // 0 for hours, 1 for borders
    }
}
