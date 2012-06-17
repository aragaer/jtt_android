package com.aragaer.ticker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/* Ticker maintains internal list of datetimes (transitions)
 * transitions are kept in Longs
 * Each transition is a start/end of an interval
 * Each interval is split into several ticks
 * Each tick is split into several subticks
 */
public abstract class Ticker {
    private static final String TAG = Ticker.class.getSimpleName();

    /* last time the ticker was run */
    private long sync;
    /* start and length of the current interval */
    private long tr0, trl;
    /* a list of future transitions in relative form
     * first element is a time to next transition from last run
     * each other is a length of a following interval
     */
    private LinkedList<Long> tr = new LinkedList<Long>();

    private int ticks; // ticks per interval
    private int subs; // subticks per tick
    private int tick, sub; // current tick and subtick number

    private double total; // ticks*subs

    public Ticker(int t, int s) {
        ticks = t;
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
        }
    };

    public final void start_ticking() {
        do_tick();
    }

    public final void stop_ticking() {
        mHandler.removeMessages(MSG);
    }

    private void do_tick() {
        boolean tr_changed = false;
        long passed, next;
        /* adjust the transition */
        do {
            passed = System.currentTimeMillis() - sync;
            Log.d(TAG, "Adjust at "+l2s(passed+sync)+", sync was at "+l2s(sync)+" "+passed+"ms passed");
            if (tr.isEmpty()) {
                /* request more items */
                exhausted();
                if (tr.isEmpty()) {
                    Log.d(TAG, "no transitions");
                    return;
                }
                passed = System.currentTimeMillis() - sync;
            }
    
            next = tr.get(0);
            Log.d(TAG, "next would be in "+next+"ms at "+l2s(sync+next));
            if (passed > next) {
                /* we can now ignore previous tick/sub values
                 * we will notify about transition anyway
                 * this allows us to simply move sync time
                 */
                sync += next;
                tr0 = sync;
                tr.remove();
                tr_changed = true;
            }
        } while (passed > next);
        Log.d(TAG, "woot");
    }

    private static final DateFormat df = new SimpleDateFormat("HH:mm:s.S");
    private static final String l2s(long t) {
        return df.format(new Date(t));
    }

    protected void init(long s) {
        sync = s;
    }

    protected void add_tr(long t) {
        long diff = t - sync;
        for (long l : tr) {
            diff -= l;
            if (diff < 0) {
                Log.d(TAG, "Value is too small");
                break;
            }
        }
        tr.addLast(diff);
    }

    /* this is called when we are past last interval */
    public abstract void exhausted();

    /* called on tick */
    public abstract void handleTick(int tick, int sub);

    /* called on subtick. Not called on tick */
    public abstract void handleSub(int tick, int sub);
}
