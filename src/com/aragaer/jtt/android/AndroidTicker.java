// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.android;

import com.aragaer.jtt.core.Clockwork;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;


public class AndroidTicker {
    public static final String ACTION_JTT_TICK = "com.aragaer.jtt.action.TICK";

    private final Context _context;
    private final Clockwork _clockwork;

    public AndroidTicker(Context context) {
        _context = context;
        _clockwork = new Clockwork(_context);
    }

    public void start() {
        long now = System.currentTimeMillis();
        _clockwork.setTime(now);

        Intent TickActionInternal = new Intent(_context, Ticker.class);

        AlarmManager am = (AlarmManager) _context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC, _clockwork.start, _clockwork.repeat,
                        PendingIntent.getService(_context, 0, TickActionInternal, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public void stop() {
        AlarmManager am = (AlarmManager) _context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(PendingIntent.getService(_context, 0, new Intent(_context, Ticker.class), 0));
    }
}
