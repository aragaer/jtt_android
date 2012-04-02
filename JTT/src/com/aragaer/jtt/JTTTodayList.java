package com.aragaer.jtt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TimeZone;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class JTTTodayList extends ListView {
    private LinkedList<Item> inner = new LinkedList<Item>();

    static final class Item {
        public final long time;
        public final int hnum;
        public final String date;

        public Item(long t, int h) {
            time = t;
            hnum = h % 12;
            date = h == -1 ? null : df.format(new Date(t));
        }
    }

    private final TodayAdapter ta;
    private final JTTMainActivity main;
    int jdn_f, jdn_l;
    private static final DateFormat df = new SimpleDateFormat("HH:mm");

    public JTTTodayList(Context context) {
        super(context);
        ta = new TodayAdapter(context, R.layout.today_item);
        setAdapter(ta);
        main = (JTTMainActivity) context;
    }

    public void addTr(long tr[]) {
        boolean day = inner.isEmpty();
        if (day) {
            inner.add(new Item(tr[0], 6)); // hour of the hare
            jdn_f = jdn_l = JTT.longToJDN(tr[0]);

            if (tr.length == 1) // nothing else left to add
                return;
            long[] tmp = new long[tr.length - 1];
            System.arraycopy(tr, 1, tmp, 0, tmp.length);
            tr = tmp;
        }
        if (tr[0] < inner.getFirst().time) {
            for (int i = tr.length - 1; i >= 0; i--) {
                add_interval(tr[i], true, day);
                day = !day;
            }
            jdn_f = JTT.longToJDN(tr[0]);
        } else {
            for (long t : tr) {
                add_interval(t, false, day);
                day = !day;
            }
            jdn_l = JTT.longToJDN(tr[tr.length - 1]);
        }
    }

    private void add_interval(long tr, boolean front, boolean day) {
        if (front) {
            long start = inner.getFirst().time;
            long diff = start - tr;
            int add = (day ? 12 : 6);
            for (int i = 1; i <= 6; i++)
                inner.addFirst(new Item(start - i * diff / 6, add - i));
        } else {
            long start = inner.getLast().time;
            long diff = tr - start;
            int add = (day ? 6 : 0);
            for (int i = 1; i <= 6; i++)
                inner.addLast(new Item(start + i * diff / 6, add + i));
        }
    }

    public void dropTrs() {
        inner.removeAll(null);
    }

    protected void onServiceConnect() {
        int jdn = JTT.longToJDN(System.currentTimeMillis());
        main.send_msg_to_service(JTTService.MSG_TRANSITIONS, jdn);
        main.send_msg_to_service(JTTService.MSG_TRANSITIONS, jdn + 1);
    }

    private void getPastDay() {
        main.send_msg_to_service(JTTService.MSG_TRANSITIONS, jdn_f - 1);
    }

    private void getFutureDay() {
        main.send_msg_to_service(JTTService.MSG_TRANSITIONS, jdn_l + 1);
    }

    private static class TodayAdapter extends ArrayAdapter<Item> {
        final HashMap<String, String> daynames = new HashMap<String, String>();
        final String[] extras;
        final JTTHour.StringsHelper sh;
        private LinkedList<Item> items = new LinkedList<Item>();

        public TodayAdapter(Context c, int layout_id) {
            super(c, layout_id);
            daynames.put("prev", c.getString(R.string.day_prev));
            daynames.put("curr", c.getString(R.string.day_curr));
            daynames.put("next", c.getString(R.string.day_next));
            sh = new JTTHour.StringsHelper(c);
            extras = new String[] { c.getString(R.string.sunset),
                    c.getString(R.string.midnight),
                    c.getString(R.string.sunrise), c.getString(R.string.midday) };
        }

        private final static void t(View v, int id, String t) {
            ((TextView) v.findViewById(id)).setText(t);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Context c = parent.getContext();
            View v = convertView;
            LayoutInflater li = (LayoutInflater) c
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            final Item item = items.get(position);
            if (item.hnum == -1) {
                v = li.inflate(android.R.layout.preference_category, null);
                t(v, android.R.id.title, dateToString(item.time, c));
            } else {
                final int h = item.hnum;
                v = li.inflate(R.layout.today_item, null);

                t(v, R.id.time, item.date);
                t(v, R.id.glyph, JTTHour.Glyphs[h]);
                t(v, R.id.name, sh.getHrOf(h));
                if (h % 3 == 0)
                    t(v, R.id.extra, extras[h / 3]);
                else
                    t(v, R.id.extra, "");
/* FIXME: works as "darken only"
                Item next = null;
                int i;
                for (i = position + 1; i < items.size(); i++) {
                    next = items.get(i);
                    if (next.hnum >= 0)
                        break;
                }
                if (i == items.size())
                    next = null;

                long now = System.currentTimeMillis();
                if (next != null
                        && (now >= item.time && now < next.time))
                    v.setBackgroundColor(android.R.color.white);
                    */
            }
            return v;
        }

        String dateToString(long date, Context c) {
            final long today = ms_to_day(System.currentTimeMillis());
            final long day = ms_to_day(date);
            int ddiff = (int) (today - day);
            switch (ddiff) {
            case 0:
                return daynames.get("curr");
            case 1:
                return daynames.get("prev");
            case -1:
                return daynames.get("next");
            default:
                final Resources r = c.getResources();
                return r.getQuantityString(ddiff > 0 ? R.plurals.days_past
                        : R.plurals.days_future, ddiff, ddiff);
            }
        }

        private static final long ms_to_day(long t) {
            t += TimeZone.getDefault().getOffset(t);
            return t / JTT.ms_per_day;
        }

        private static final long align_to_day(long o) {
            long n = ms_to_day(o) * JTT.ms_per_day;
            n -= TimeZone.getDefault().getOffset(n);
            return n;
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
