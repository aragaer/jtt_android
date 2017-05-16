// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.android;

import java.text.SimpleDateFormat;

import com.aragaer.jtt.core.*;
import com.aragaer.jtt.astronomy.*;
import com.aragaer.jtt.JttService;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;


public class AndroidTicker extends Handler {
    public static final String ACTION_JTT_TICK = "com.aragaer.jtt.action.TICK";

    private final Context _context;
    private final Clockwork _clockwork;

    public AndroidTicker(Context context, Clockwork clockwork) {
        _context = context;
        _clockwork = clockwork;
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
        handle(now);
    }

    private void handle(long now) {
        SolarEventCalculator calculator = SscAdapter.getInstance();
        IntervalProvider provider = new SscCalculator(calculator);
        ThreeIntervals intervals = provider.getIntervalsForTimestamp(now);

        if (intervals.surrounds(now)) {
            Hour hour = Hour.fromInterval(intervals.getMiddleInterval(), now);
            Intent TickAction = new Intent(AndroidTicker.ACTION_JTT_TICK)
                .putExtra("intervals", intervals)
                .putExtra("hour", hour.num)
                .putExtra("jtt", hour.wrapped);
            _context.sendStickyBroadcast(TickAction);

            if (PreferenceManager.getDefaultSharedPreferences(_context)
                .getBoolean("jtt_notify", true))
                _context.startService(new Intent(_context, JttService.class));

            Log.d("JTT CLOCKWORK", "Tick: "+hour.num+":"+hour.quarter+":"+hour.tick);
        } else
            try {
                start();
            } catch (IllegalStateException e) {
                Log.i("JTT CLOCKWORK", "Transition passed while service is not running, ignore");
            }
    }
}
