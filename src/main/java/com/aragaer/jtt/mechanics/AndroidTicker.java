// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.mechanics;

import java.text.SimpleDateFormat;

import com.aragaer.jtt.core.Clockwork;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class AndroidTicker extends Handler implements Ticker {
    public static final String ACTION_JTT_TICK = "com.aragaer.jtt.action.TICK";

    private final Context _context;
    private final Clockwork _clockwork;
    private final Announcer _announcer;

    public AndroidTicker(Context context, Clockwork clockwork, Announcer announcer) {
        _context = context;
        _clockwork = clockwork;
        _announcer = announcer;
    }

    public void start() {
        sendEmptyMessage(0);
    }

    public void stop() {
        removeMessages(0);
    }

    @Override public void handleMessage(Message msg) {
        removeMessages(0);
        Log.d("JTT CLOCKWORK", "Handler ticked");
        long now = System.currentTimeMillis();
        _clockwork.setTime(now);

        long ms_passed = now - _clockwork.start;
        int ticks_passed = (int) (ms_passed / _clockwork.repeat);
        long next_tick = (ticks_passed + 1) * _clockwork.repeat + _clockwork.start;
        long delay = next_tick - now;
        sendEmptyMessageDelayed(0, delay);
        Log.d("JTT CLOCKWORK", "Tick delay " + delay);
        Log.d("JTT CLOCKWORK", "Next tick scheduled at "+(new SimpleDateFormat("HH:mm:ss.SSS").format(next_tick)));
        _announcer.announce(now);
    }
}
