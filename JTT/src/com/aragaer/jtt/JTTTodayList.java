package com.aragaer.jtt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class JTTTodayList extends ListView {
    private static final String TAG = JTTTodayList.class.getSimpleName();

    private LinkedList<TodayItem> inner = new LinkedList<TodayItem>();
    private LinkedList<Long> transitions = new LinkedList<Long>();

    /* sets a value to a text field */
    private final static void t(View v, int id, String t) {
        ((TextView) v.findViewById(id)).setText(t);
    }

    /* A single item in TodayList */
    private static class TodayItem {
        public final long time;
        public final int hnum;
        public final String date;

        public TodayItem(long t, int h) {
            time = t;
            hnum = h % 12;
            date = h == -1 ? null : df.format(new Date(t));
        }

        static Context ctx = null;
        static String[] extras = null;
        static JTTHour.StringsHelper sh = null;

        protected void updateCtx(Context c) {
            /* as long as only one context is used, keep these objects */
            if (!c.equals(ctx)) {
                sh = new JTTHour.StringsHelper(c);
                extras = new String[] { c.getString(R.string.sunset), "", "",
                        c.getString(R.string.midnight), "", "",
                        c.getString(R.string.sunrise), "", "",
                        c.getString(R.string.midday), "", "" };
                ctx = c;
            }
        }

        public View toView(Context c) {
            updateCtx(c);
            LayoutInflater li = (LayoutInflater) c
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = li.inflate(R.layout.today_item, null);
            boolean is_current = false; // TODO: fix this!

            t(v, R.id.time, date);
            t(v, R.id.glyph, JTTHour.Glyphs[hnum]);
            t(v, R.id.name, sh.getHrOf(hnum));
            t(v, R.id.extra, extras[hnum]);
            t(v, R.id.curr, is_current ? "▶" : "");

            return v;
        }
    }

    /* "DayName" item in Today List */
    private static class DayItem extends TodayItem {
        static String[] daynames = null;
        public DayItem(long t) {
            super(t, -1);
        }

        @Override
        protected void updateCtx(Context c) {
            if (!c.equals(ctx))
                daynames = new String[] { c.getString(R.string.day_next),
                        c.getString(R.string.day_curr),
                        c.getString(R.string.day_prev) };
            super.updateCtx(c);
        }

        @Override
        public View toView(Context c) {
            updateCtx(c);
            LayoutInflater li = (LayoutInflater) c
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = li.inflate(android.R.layout.preference_category, null);
            t(v, android.R.id.title, dateToString(time, c));

            return v;
        }

        /* returns strings like "today" or "2 days ago" etc */
        String dateToString(long date, Context c) {
            final long today = ms_to_day(System.currentTimeMillis());
            final int ddiff = (int) (today - ms_to_day(date));
            if (ddiff < 2 && ddiff > -2)
                return daynames[ddiff+1];
            final Resources r = c.getResources();
            return r.getQuantityString(ddiff > 0
                ? R.plurals.days_past
                : R.plurals.days_future, ddiff, ddiff);
        }
    }

    private final TodayAdapter ta;
    private final JTTMainActivity main;
    long jdn_f, jdn_l;
    private static final DateFormat df = new SimpleDateFormat("HH:mm");
    private long forward_sync = 0, backward_sync = 0;

    public JTTTodayList(Context context) {
        super(context);
        ta = new TodayAdapter(context, R.layout.today_item);
        setAdapter(ta);
        main = (JTTMainActivity) context;
    }

    /* handle bundle containing another couple of transitions
     * if list is empty at the moment, we have only one day
     * that starts with sunrise and ends with sunset
     * if list is not empty, we have a new night and a day
     * these two go to the beginning or to the end of the list
     */
    public void addTransitions(Bundle b) {
        boolean forward;
        long jdn = b.getLong("jdn");
        long sunrise = b.getLong("sunrise");
        long sunset = b.getLong("sunset");

        if (inner.isEmpty()) {
            forward = true;
            inner.add(new TodayItem(sunrise, 6));
            jdn_l = jdn_f = jdn;
        } else { // list is not empty, add one night to list
            if (jdn <= jdn_l && jdn >= jdn_f) {
                Log.wtf(TAG, "Got "+jdn+" which is between "+jdn_f+" and "+jdn_l);
                return;
            }
            forward = jdn_l < jdn;
            add_interval(forward ? sunrise : sunset, !forward, false);
        }

        // now add one day to list
        if (forward) {
            jdn_l = jdn;
            add_interval(sunset, false, true);
        } else {
            jdn_f = jdn;
            add_interval(sunrise, true, true);
        }
        updateItems();
    }

    /* split the interval into 6 equal parts and add them to the list */
    private void add_interval(long tr, boolean to_front, boolean day) {
        if (to_front) {
            long start = inner.getFirst().time;
            long diff = start - tr;
            int add = (day ? 12 : 6);
            for (int i = -1; i >= -6; i--)
                inner.addFirst(new TodayItem(start + i * diff / 6, add + i));
        } else {
            long start = inner.getLast().time;
            long diff = tr - start;
            int add = (day ? 6 : 0);
            for (int i = 1; i <= 6; i++)
                inner.addLast(new TodayItem(start + i * diff / 6, add + i));
        }
    }

    public void reset() {
        inner.clear();
    }

    protected void onServiceConnect() {
        getDay(JTT.longToJDN(System.currentTimeMillis()));
    }

    /* request transitions for given day from JTTService */
    private void getDay(long jdn) {
        Bundle b = new Bundle();
        b.putLong("jdn", jdn);
        Log.d(TAG, "Requesting transitions for day "+jdn);
        main.send_msg_to_service(JTTService.MSG_TRANSITIONS, b);
    }

    private void getPastDay() {
        getDay(jdn_f - 1);
    }

    private void getFutureDay() {
        getDay(jdn_l + 1);
    }

    // TODO: remove obsolete items
    private static final int PAD = 6;
    private void updateItems() {
        // first find out to which interval the current time belongs
        long now = System.currentTimeMillis();
        final int is = inner.size();
        if (inner.get(PAD).time > now) {
            getPastDay();
            return;
        }
        if (inner.get(is - PAD - 1).time < now) {
            getFutureDay();
            return;
        }
        int i = PAD + 1;
        while (inner.get(i).time < now)
            i++;
        int low = (i - PAD - 1) / 6 * 6;
        int high = (i + PAD + 5) / 6 * 6;
        ta.buildItems(inner.subList(low, high + 1));
    }

    public void setCurrent(int cur) {
        long now = System.currentTimeMillis();
        ta.cur = cur;
        /* if everything is in valid state, that's simple */
        if (now < forward_sync && now >= backward_sync) {
            ta.notifyDataSetChanged();
            return;
        }

        // here's some redrawing
        ta.notifyDataSetChanged();
    }

    private static class TodayAdapter extends ArrayAdapter<TodayItem> {
        protected int cur;

        public TodayAdapter(Context c, int layout_id) {
            super(c, layout_id);
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            return getItem(position).toView(parent.getContext());
        }

        /* takes a sublist of hours
         * creates a list to display by adding day names
         */
        private void buildItems(List<TodayItem> in) {
            clear();

            /* day is time stamp for 00:00:00 */
            long day = in.get(0).time;

            /* "aligning" code */
            day += TimeZone.getDefault().getOffset(day);
            day -= day % JTT.ms_per_day;
            day -= TimeZone.getDefault().getOffset(day);

            for (TodayItem i : in) {
                if (i.time >= day) {
                    add(new DayItem(day));
                    day += JTT.ms_per_day;
                }
                add(i);
            }
        }

        @Override
        public boolean isEnabled(int pos) {
            return false;
        }
    }

    /* helper function
     * converts ms timestamp to day number
     * not useful by itself but can be used to find difference between days
     */
    private static final long ms_to_day(long t) {
        t += TimeZone.getDefault().getOffset(t);
        return t / JTT.ms_per_day;
    }
}
