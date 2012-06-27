package com.aragaer.jtt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
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
    private long next_sync = 0, prev_sync = 0;

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
            jdn_l = JTT.longToJDN(tr[0]);
            jdn_f = jdn_l + 1;

            if (tr.length == 1) // nothing else left to add
                Log.e("today", "just a single tr!"); // should never happen!
            long[] tmp = new long[tr.length - 1];
            System.arraycopy(tr, 1, tmp, 0, tmp.length);
            tr = tmp;
        }
        if (tr[tr.length - 1] < inner.getFirst().time) {
            for (int i = tr.length - 1; i >= 0; i--) {
                add_interval(tr[i], true, day);
                day = !day;
            }
            jdn_f = JTT.longToJDN(tr[0]);
        } else if (tr[0] > inner.getLast().time) {
            for (long t : tr) {
                add_interval(t, false, day);
                day = !day;
            }
            jdn_l = JTT.longToJDN(tr[tr.length - 1]);
        } else
            Log.w("today", "We already have these transitions!");
        updateItems();
    }

    private void add_interval(long tr, boolean front, boolean day) {
        if (front) {
            long start = inner.getFirst().time;
            long diff = start - tr;
            int add = (day ? 12 : 6);
            for (int i = -1; i >= -6; i--)
                inner.addFirst(new Item(start + i * diff / 6, add + i));
        } else {
            long start = inner.getLast().time;
            long diff = tr - start;
            int add = (day ? 6 : 0);
            for (int i = 1; i <= 6; i++)
                inner.addLast(new Item(start + i * diff / 6, add + i));
        }
    }

    public void dropTrs() {
        inner.clear();
    }

    protected void onServiceConnect() {
        int jdn = JTT.longToJDN(System.currentTimeMillis());
        main.send_msg_to_service(JTTService.MSG_TRANSITIONS, jdn);
    }

    private void getPastDay() {
        main.send_msg_to_service(JTTService.MSG_TRANSITIONS, jdn_f - 1);
    }

    private void getFutureDay() {
        main.send_msg_to_service(JTTService.MSG_TRANSITIONS, jdn_l + 1);
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
        // FIXME: next and prev syncs have to be on transitions, not hours!
        next_sync = inner.get(i).time;
        prev_sync = inner.get(i - 1).time;
        int low = (i - PAD - 1) / 6 * 6;
        int high = (i + PAD + 5) / 6 * 6;
        ta.buildItems(inner.subList(low, high + 1));
    }

    public void setCurrent(int cur) {
        long now = System.currentTimeMillis();
        ta.cur = cur;
        if (now >= next_sync)
            getFutureDay();
        else if (now < prev_sync) // time went backwards
            getPastDay();
        else
            ta.notifyDataSetChanged();
    }

    private static class TodayAdapter extends ArrayAdapter<Item> {
        final String[] daynames, extras;
        final JTTHour.StringsHelper sh;
        private LinkedList<Item> items = new LinkedList<Item>();
        protected int cur;

        public TodayAdapter(Context c, int layout_id) {
            super(c, layout_id);
            daynames = new String[] { c.getString(R.string.day_next),
                    c.getString(R.string.day_curr),
                    c.getString(R.string.day_prev) };
            sh = new JTTHour.StringsHelper(c);
            extras = new String[] { c.getString(R.string.sunset), "", "",
                    c.getString(R.string.midnight), "", "",
                    c.getString(R.string.sunrise), "", "",
                    c.getString(R.string.midday), "", "" };
        }

        /* sets a value to a text field */
        private final static void t(View v, int id, String t) {
            ((TextView) v.findViewById(id)).setText(t);
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            final Context c = parent.getContext();
            LayoutInflater li = (LayoutInflater) c
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            final Item item = items.get(position);
            final int h = item.hnum;

            if (h == -1) {
                v = li.inflate(android.R.layout.preference_category, null);
                t(v, android.R.id.title, dateToString(item.time, c));
            } else {
                v = li.inflate(R.layout.today_item, null);

                t(v, R.id.time, item.date);
                t(v, R.id.glyph, JTTHour.Glyphs[h]);
                t(v, R.id.name, sh.getHrOf(h));
                t(v, R.id.extra, extras[h]);

                /* if current hour is sunrise or sunset
                 * then last hour in list has the same hnum
                 * do not mark it as current in this case
                 */
                if (item.hnum == cur && position < items.size() - 1)
                    t(v, R.id.curr, "▶");
                else
                    t(v, R.id.curr, "");
            }
            return v;
        }

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

        private static final long ms_to_day(long t) {
            t += TimeZone.getDefault().getOffset(t);
            return t / JTT.ms_per_day;
        }

        private static final long align_to_day(long o) {
            long n = ms_to_day(o) * JTT.ms_per_day;
            return n - TimeZone.getDefault().getOffset(n);
        }

        private void buildItems(List<Item> in) {
            items.clear();

            long day = align_to_day(in.get(0).time);
            for (Item i : in) {
                if (i.time >= day) {
                    items.add(new Item(day, -1));
                    day += JTT.ms_per_day;
                }
                items.add(i);
            }

            notifyDataSetChanged();
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
