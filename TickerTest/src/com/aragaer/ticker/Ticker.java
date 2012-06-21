package com.aragaer.ticker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/* Ticker maintains internal list of datetimes (transitions)
 * transitions are kept in Longs
 * Each transition is a start/end of an interval
 * Each interval is split into several ticks
 * Each tick is split into several subticks
 * Ticker calls handle_sub on each subtick, handle_tick on each tick
 * If several subticks passed between invocations, handle_sub is called only once
 * If tick passed between invocations, only handle_tick is called once (no handle_sub)
 */
public abstract class Ticker {
    private static final String TAG = Ticker.class.getSimpleName();
    
    public static int KEEP_TICKING = 0;
    public static int STOP_TICKING = 1;

    /* last time the ticker was run */
    private long sync = 0;
    /* a list of future transitions in relative form
     * first element is a time to next transition from last run
     * each other is a length of a following interval
     */
    private ArrayList<Long> tr = new ArrayList<Long>();

//    private int ticks;  // ticks per interval, not used directly
    private int subs;   // subticks per tick
    private int tick, sub; // current tick and subtick number

    long start, end;        // start and end of current interval
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

    public void start_ticking() {
        wake_up();
    }

    public void stop_ticking() {
        mHandler.removeMessages(MSG);
    }

    /* This function assumes that we have just woke up
     * Do not attempt to short-cut any calculations based on previous runs
     */
    private void wake_up() {
        long now;
        
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

        long offset = now - start;
        double sublen = ((double) (end - start))/total;
        int exp_total = (int) (offset/sublen);
        int exp_tick = exp_total / subs;
        int exp_sub = exp_total % subs;
        long next_sub = start + Math.round(sublen * (exp_total + 1));

        if (now - sync > exp_sub * sublen // sync belongs to previous tick interval
                || now < sync) {          // time went backwards!
            tick = exp_tick;
            sub = exp_sub;
            handle_tick(tick, sub);
        } else if (sub < exp_sub) {
            if (tick != exp_tick) { // sync should belong to this tick interval
                Log.wtf(TAG, "current tick is "+tick+", expected "+exp_tick);
                tick = exp_tick;
            }
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
        if (pos < 0) // ignore duplicates
            tr.add(-1-pos, t);
    }

    /* this is called when we are past last interval */
    protected abstract int overrun();

    /* this is called when we are before first */
    protected abstract int underrun();

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
