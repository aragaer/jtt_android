package com.aragaer.jtt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.LauncherActivity.ListItem;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class JTTTodayList extends ListView {
    private final TodayAdapter ta;
    private final JTTMainActivity main;

    public JTTTodayList(Context context) {
        super(context);
        ta = new TodayAdapter(context, R.layout.today_item);
        main = (JTTMainActivity) getParent();
    }

    public void refresh() {
        setAdapter(ta);
    }

    private static class TodayAdapter extends ArrayAdapter<Date> {
        final String[] daynames;
        final String[] extras;
        final JTTHour.StringsHelper sh;
        // 19 hour records
        private ArrayList<Object> items = new ArrayList<Object>();
        private ArrayList<Date> dates = new ArrayList<Date>();
        private boolean isNight;

        public TodayAdapter(Context c, int layout_id) {
            super(c, layout_id);
            daynames = new String[] { c.getString(R.string.day_prev),
                    c.getString(R.string.day_curr),
                    c.getString(R.string.day_next) };
            sh = new JTTHour.StringsHelper(c);
            extras = new String[] { c.getString(R.string.sunset),
                    c.getString(R.string.midnight),
                    c.getString(R.string.sunrise), c.getString(R.string.midday) };
        }

        private final static void t(View v, int id, String t) {
            ((TextView) v.findViewById(id)).setText(t);
        }

        private static final DateFormat df = new SimpleDateFormat("HH:mm");

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            LayoutInflater li = (LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            final Object item = items.get(position);
            if (item instanceof Integer) {
                final int h = ((Integer) item).intValue();
                final int hpos = (h + (isNight ? 6 : 0)) % 12;
                v = li.inflate(R.layout.today_item, null);

                t(v, R.id.time, df.format(dates.get(h)));
                t(v, R.id.glyph, JTTHour.Glyphs[hpos]);
                t(v, R.id.name, sh.getHrOf(hpos));
                if (hpos % 3 == 0)
                    t(v, R.id.extra, extras[hpos / 3]);
                else
                    t(v, R.id.extra, "");

            } else {
                v = li.inflate(android.R.layout.preference_category, null);
                t(v, android.R.id.title, (String) item);
            }
            return v;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public boolean isEnabled(int pos) {
            return false;
        }
    }
}
