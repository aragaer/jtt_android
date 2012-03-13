package com.aragaer.jtt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

    public JTTTodayList(Context context) {
        super(context);
        ta = new TodayAdapter(context, R.layout.today_item);
    }

    public void set_transitions(Bundle b) {
        ta.setDates(b);
        setAdapter(ta);
    }

    private static class TodayAdapter extends ArrayAdapter<Date> {
        static final String[] night = { "prev_sunrise", "prev_sunset",
                "next_sunrise", "next_sunset" };
        static final String[] day = { "prev_sunset", "prev_sunrise",
                "next_sunset", "next_sunrise" };
        final String[] daynames;
        final String[] extras;
        final JTTHour.StringsHelper sh;
        private int[] day_pos = new int[2];
        private int day_count;
        // 19 hour records
        private ArrayList<Date> dates = new ArrayList<Date>(19);
        private Boolean isNight;

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

        public void setDates(Bundle b) {
            isNight = b.getBoolean("is_night");
            ArrayList<Long> times = new ArrayList<Long>(4);
            for (String s : isNight ? night : day)
                times.add(b.getLong(s));
            long s, e = times.remove(0);
            day_count = 0;

            dates.clear();

            int last_date = 99; // something impossible
            for (int i = 0; i < 3; i++) {
                s = e;
                e = times.remove(0);
                long diff = e - s;
                for (int j = 0; j < 6; j++) {
                    Date d = new Date(s + diff * j / 6);
                    int date = d.getDate();
                    if (date != last_date) {
                        day_pos[day_count] = dates.size() + day_count;
                        day_count++;
                        last_date = date;
                    }
                    dates.add(d);
                }
            }
            dates.add(new Date(e));
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

            Boolean isHour = true;
            int d = 0;
            for (int p : day_pos) {
                if (p < position) {
                    d++;
                    continue;
                }

                isHour = p != position;
                break;
            }
            if (isHour) {
                final int hpos = (position - d + (isNight ? 6 : 0)) % 12;
                v = li.inflate(R.layout.today_item, null);

                t(v, R.id.time, df.format(dates.get(position - d)));
                t(v, R.id.glyph, JTTHour.Glyphs[hpos]);
                t(v, R.id.name, sh.getHrOf(hpos));
                if (hpos % 3 == 0)
                    t(v, R.id.extra, extras[hpos / 3]);
                else
                    t(v, R.id.extra, "");

            } else {
                v = li.inflate(android.R.layout.preference_category, null);
                t(v, android.R.id.title, daynames[d]);
            }
            return v;
        }

        @Override
        public int getCount() {
            return /* dates.size() */ 19 + day_count;
        }
    }
}
