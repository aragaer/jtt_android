// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.android;

import com.aragaer.jtt.core.*;

import android.app.*;
import android.content.*;
import android.util.Log;


public class AndroidTicker extends IntentService {
    public static final String ACTION_JTT_TICK = "com.aragaer.jtt.action.TICK";
    private static final Intent TickAction = new Intent(ACTION_JTT_TICK);

    public AndroidTicker() {
        super("CLOCKWORK");
    }

    public static void schedule(final Context context) {
        long now = System.currentTimeMillis();
        Clockwork starter = new Clockwork(context, now);

        Intent TickActionInternal = new Intent(context, AndroidTicker.class)
            .putExtra("intervals", starter.data);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC, starter.start, starter.repeat,
                        PendingIntent.getService(context, 0, TickActionInternal, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public static void unschedule(final Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(PendingIntent.getService(context, 0, new Intent(context, AndroidTicker.class), 0));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ThreeIntervals intervals = (ThreeIntervals) intent.getSerializableExtra("intervals");
        long now = System.currentTimeMillis();
        Hour hour = Hour.fromInterval(intervals.getMiddleInterval(), now, null);

        if (intervals.surrounds(now)) {
            Intent TickAction = new Intent(ACTION_JTT_TICK)
                .putExtra("intervals", intervals)
                .putExtra("hour", hour.num)
                .putExtra("jtt", hour.wrapped);
            sendStickyBroadcast(TickAction);
        } else
            try {
                schedule(this);
            } catch (IllegalStateException e) {
                Log.i("JTT CLOCKWORK", "Transition passed while service is not running, ignore");
            }

        stopSelf();
    }
}
