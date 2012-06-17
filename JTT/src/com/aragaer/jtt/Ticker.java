package com.aragaer.jtt;

import java.util.Date;
import java.util.LinkedList;

import android.os.Handler;
import android.os.Message;

public abstract class Ticker {
    protected final boolean round;

    public Date start, end;
    protected LinkedList<Long> tr = new LinkedList<Long>();

    private final static int MSG = 1;
    private long rate; // number of millis per 1% of hour
    private int ticks, subs, tick, sub;
    private double total;

    public Ticker(int ticks, int subs) {
        this(ticks, subs, true);
    }

    public Ticker(int t, int s, boolean r) {
        ticks = t;
        subs = s;
        total = t * s;
        round = r;
    }

    public void reset() {
        tr.clear();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            synchronized (Ticker.this) {
                long lastTickStart = System.currentTimeMillis();
                int o_tick = tick;
                if (++sub >= subs) {
                    sub %= subs;
                    if (++tick >= ticks)
                        lastTickStart += resync_tick();
                    else {
                        start = end;
                        end = new Date(start.getTime() + subs * rate);
                    }
                }

                if (o_tick == tick)
                    handleSub(tick, sub);
                else
                    handleTick(tick, sub);

                // take into account user's onTick taking time to execute
                long delay = lastTickStart - System.currentTimeMillis() + rate;
                // special case: user's onTick took more than interval to
                // complete, skip to next interval
                while (delay <= 0) {
                    delay += rate;
                    sub++;
                }

                sendMessageDelayed(obtainMessage(MSG), delay);
            }
        }
    };

    public final void stop_ticking() {
        mHandler.removeMessages(MSG);
    }

    public synchronized final void start_ticking() {
        long delay = resync_tick();
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG), delay);
        handleTick(tick, sub);
    }

    private void update_to(long ms) {
        int s = tr.size();
        do {
            while (s > 1) {
                if (ms < tr.get(1))
                    return;
                tr.remove();
                s--;
            }
            exhausted();
        } while ((s = tr.size()) > 1);
    }

    /* updates rate, recalculates now, returns delay to next tick */
    private long resync_tick() {
        long ms = System.currentTimeMillis();
        update_to(ms);
        final long tr0 = tr.get(0);
        final long tr1 = tr.get(1);
        rate = Math.round((tr1 - tr0) / total);
        double h = total * (ms - tr0) / (tr1 - tr0) + (round ? 0.5 : 0);
        tick = (int) h / subs;
        sub = (int) h % subs;
        final long start_ms = tr0 + rate * subs * tick;
        start = new Date(start_ms);
        end = new Date(start_ms + rate * subs);
        return rate - (ms - tr0) % rate;
    }

    public abstract void exhausted();

    public abstract void handleTick(int tick, int sub);

    public abstract void handleSub(int tick, int sub);
}
