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

    public static void schedule(final Context context) {
        long now = System.currentTimeMillis();
        Clockwork starter = new Clockwork(context, now);

        Intent TickActionInternal = new Intent(context, Ticker.class)
            .putExtra("intervals", starter.data);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC, starter.start, starter.repeat,
                        PendingIntent.getService(context, 0, TickActionInternal, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public static void unschedule(final Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(PendingIntent.getService(context, 0, new Intent(context, Ticker.class), 0));
    }
}
