// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import com.aragaer.jtt.android.AndroidTicker;
import com.aragaer.jtt.core.*;
import com.aragaer.jtt.resources.RuntimeResources;
import com.aragaer.jtt.resources.StringResources;
import com.aragaer.jtt.resources.StringResources.StringResourceChangeListener;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.widget.RemoteViews;


public class JttStatus extends BroadcastReceiver implements StringResourceChangeListener {
    private static final int APP_ID = 0;

    private final Context context;
    private final StringResources sr;
    private Hour h = new Hour(0);
    private long start, end;
    private final NotificationManager nm;

    public JttStatus(final Context ctx) {
        context = ctx;
        nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        sr = RuntimeResources.get(context).getInstance(StringResources.class);
        sr.registerStringResourceChangeListener(this,
                                                StringResources.TYPE_HOUR_NAME | StringResources.TYPE_TIME_FORMAT);

        context.registerReceiver(this, new IntentFilter(AndroidTicker.ACTION_JTT_TICK));
    }

    public void release() {
        nm.cancel(APP_ID);
        sr.unregisterStringResourceChangeListener(this);
        context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        final String action = intent.getAction();
        if (!action.equals(AndroidTicker.ACTION_JTT_TICK))
            return;

        setIntervals((ThreeIntervals) intent.getSerializableExtra("intervals"));
    }

    public void setIntervals(ThreeIntervals intervals) {
        Interval currentInterval = intervals.getMiddleInterval();
        h = Hour.fromInterval(currentInterval, System.currentTimeMillis());
        final long tr[] = intervals.getTransitions();
        final int lower = Hour.lowerBoundary(h.num),
            upper = Hour.upperBoundary(h.num);
        start = Hour.getHourBoundary(currentInterval.start, currentInterval.end, lower);
        end = Hour.getHourBoundary(currentInterval.start, currentInterval.end, upper);
        if (end < start) {// Cock or Hare
            if (h.quarter >= 2) // we've passed the transition
                start = Hour.getHourBoundary(tr[0], tr[1], lower);
            else
                end = Hour.getHourBoundary(tr[2], tr[3], upper);
        }

        show();
    }

    private void show() {
        nm.notify(APP_ID, buildNotification());
    }

    public Notification buildNotification() {
        int hf = h.quarter * Hour.TICKS_PER_QUARTER + h.tick;
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.notification);

        rv.setTextViewText(R.id.image, Hour.Glyphs[h.num]);
        rv.setTextViewText(R.id.title, sr.getHrOf(h.num));
        rv.setTextViewText(R.id.quarter, sr.getQuarter(h.quarter));
        rv.setProgressBar(R.id.fraction, Hour.TICKS_PER_HOUR, hf, false);
        rv.setProgressBar(R.id.fraction, Hour.TICKS_PER_HOUR, hf, false);
        rv.setTextViewText(R.id.start, sr.format_time(start));
        rv.setTextViewText(R.id.end, sr.format_time(end));

        return new Notification.Builder(context)
            .setContent(rv)
            .setOngoing(true)
            .setSmallIcon(R.drawable.notification_icon, h.num)
            .setContentIntent(PendingIntent.getActivity(context, 0,
                                                        new Intent(context, JTTMainActivity.class), 0))
            .getNotification();
    }

    public void onStringResourcesChanged(final int changes) {
        show();
    }
}
