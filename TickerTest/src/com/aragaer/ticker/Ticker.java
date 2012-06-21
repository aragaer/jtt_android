package com.aragaer.ticker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/* Ticker maintains internal list of datetimes (transitions)
 * transitions are kept in Longs
 * Each transition is a start/end of an interval
 * Each interval is split into several ticks
 * Each tick is split into several subticks
 */
public abstract class Ticker {
    @SuppressWarnings("unused")
    private static final String TAG = Ticker.class.getSimpleName();
    
    public static int KEEP_TICKING = 0;
    public static int STOP_TICKING = 1;

    /* last time the ticker was run */
    private long sync = System.currentTimeMillis();
    /* a list of future transitions in relative form
     * first element is a time to next transition from last run
     * each other is a length of a following interval
     */
    private ArrayList<Long> tr = new ArrayList<Long>();

//    private int ticks; // ticks per interval
                         // not used directly
    private int subs; // subticks per tick
    private int tick, sub; // current tick and subtick number

    private double total; // ticks*subs

    public Ticker(int t, int s) {
//        ticks = t;
        subs = s;
        total = t * s;
    }

    /* drop all existing data */
    public void reset() {
        tr.clear();
    }

    private final static int MSG = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            wake_up();
        }
    };

    public final void start_ticking() {
        wake_up();
    }

    public final void stop_ticking() {
        mHandler.removeMessages(MSG);
    }

    private void wake_up() {
        long start, end, now;
        
        /* do not want more than one message being in the system */
        mHandler.removeMessages(MSG);
        while (true) { // we are likely to pass this loop only once
            int len = tr.size();
            now = System.currentTimeMillis();
            int pos = Collections.binarySearch(tr, now);
            /*
             * Possible results:
             * -len - 1:        overrun
             * -len - 2 to -2:  OK (tr[-pos - 2] < tr[-pos - 1])
             * -1:              underrun
             * 0 to len - 2:    OK (tr[pos] == now < tr[pos + 1])
             * len - 1:         overrun
             *
             * if len == 0, the only result is -1
             * if len == 1, there's no OK results
             */
            if (pos == -1) { // right before first element
                if (underrun() == KEEP_TICKING)
                    continue;
                else
                    return;
            }
            if (pos == -len - 1             // after the last element
                    || pos == len - 1) {    // equal to the last element 
                if (overrun() == KEEP_TICKING)
                    continue;
                else
                    return;
            }
            if (pos < 0)
                pos = -pos - 2;

            start = tr.get(pos);
            end = tr.get(pos + 1);
            break;
        }
        /* great, we have start and end now */

        long offset = now - start;
        double sublen = (end - start)/total;
        int exp_total = (int) (offset/sublen);
        int exp_tick = exp_total / subs;
        int exp_sub = exp_total % subs;
        long next_sub = start + Math.round(sublen * (exp_total + 1));

        // do not use exp_tick since we might be in a different tr
        if (now - sync > exp_sub * sublen || now < sync) {
            tick = exp_tick;
            sub = exp_sub;
            handle_tick(tick, sub);
        } else if (sub < exp_sub) {
            if (tick != exp_tick) // we might have just started
                tick = exp_tick;
            sub = exp_sub;
            handle_sub(tick, sub);
        }

        sync = System.currentTimeMillis();
        if (sync < next_sub)
            mHandler.sendEmptyMessageDelayed(MSG, next_sub - sync);
        else
            mHandler.sendEmptyMessage(MSG);
    }

    private static final DateFormat df = new SimpleDateFormat("HH:mm:s.S");
    @SuppressWarnings("unused")
    private static final String l2s(long t) {
        return df.format(new Date(t));
    }

    protected void add_tr(long t) {
        int pos = Collections.binarySearch(tr, t);
        if (pos >= 0)
            return; // ignore duplicates
        if (-1-pos == tr.size())
            tr.add(t);
        else
            tr.add(-1-pos, t);
    }

    /* this is called when we are past last interval */
    public abstract int overrun();

    /* this is called when we are before first */
    public abstract int underrun();

    /* called on tick */
    public abstract void handle_tick(int tick, int sub);

    /* called on subtick. Not called on tick */
    public abstract void handle_sub(int tick, int sub);

    /* serialize to bundle */
    public void save_to_bundle(Bundle save, String key) {
        long[] t = new long[tr.size()];
        for (int i = 0; i < tr.size(); i++)
            t[i] = tr.get(i);
        save.putLongArray(key, t);
    }

    /* deserialize from bundle */
    public void load_from_bundle(Bundle save, String key) {
        long st[] = save.getLongArray(key);
        if (st != null)
            for (long t : st)
                tr.add(t);
    }
}
