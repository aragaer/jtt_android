// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import com.aragaer.jtt.android.JttApplication;
import com.aragaer.jtt.core.*;
import com.aragaer.jtt.mechanics.AndroidTicker;
import com.aragaer.jtt.resources.StringResources;
import com.aragaer.jtt.resources.StringResources.StringResourceChangeListener;

import android.app.*;
import android.content.*;
import androidx.core.app.NotificationCompat;

import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;


public class JttStatus extends Service implements StringResourceChangeListener {
    private static final int APP_ID = 1;
    private static final String CHANNEL_ID = "jtt_notification_channel";

    private Hour h = new Hour(0);
    private long start, end;

    @Override public IBinder onBind(Intent intent) {
        return null;
    }

    @Override public void onDestroy() {
        stopForeground(true);
        StringResources sr = ((JttApplication) getApplicationContext()).getStringResources();
        sr.unregisterStringResourceChangeListener(this);
        unregisterReceiver(_tick);
        deleteNotificationChannel();
    }

    @Override public int onStartCommand(Intent intent, int flags, int startid) {
        StringResources sr = ((JttApplication) getApplicationContext()).getStringResources();
        createNotificationChannel();
        sr.registerStringResourceChangeListener(this,
                StringResources.TYPE_HOUR_NAME | StringResources.TYPE_TIME_FORMAT);
        registerReceiver(_tick, new IntentFilter(AndroidTicker.ACTION_JTT_TICK));
        show();
        return START_STICKY;
    }

    private final BroadcastReceiver _tick = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (!AndroidTicker.ACTION_JTT_TICK.equals(action))
                return;

            ThreeIntervals data = (ThreeIntervals) intent.getSerializableExtra("intervals");
            if (data == null)
                return;
            Hour hour = Hour.fromTickNumber(intent.getIntExtra("jtt", 0));
            setIntervals(data, hour);
        }
    };

    private void setIntervals(ThreeIntervals intervals, Hour hour) {
        Interval currentInterval = intervals.getMiddleInterval();
        h = hour;
        final long[] tr = intervals.getTransitions();
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
        startForeground(APP_ID, buildNotification());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        CharSequence name = getString(R.string.app_name);
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        nm.createNotificationChannel(channel);
    }

    private void deleteNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.deleteNotificationChannel(CHANNEL_ID);
    }

    private Notification buildNotification() {
        StringResources sr = ((JttApplication) getApplicationContext()).getStringResources();
        int hf = h.quarter * Hour.TICKS_PER_QUARTER + h.tick;
        RemoteViews rv = new RemoteViews(getPackageName(), R.layout.notification);

        rv.setTextViewText(R.id.image, Hour.Glyphs[h.num]);
        rv.setTextViewText(R.id.title, sr.getHrOf(h.num));
        rv.setTextViewText(R.id.quarter, sr.getQuarter(h.quarter));
        rv.setProgressBar(R.id.fraction, Hour.TICKS_PER_HOUR, hf, false);
        rv.setProgressBar(R.id.fraction, Hour.TICKS_PER_HOUR, hf, false);
        rv.setTextViewText(R.id.start, sr.format_time(start));
        rv.setTextViewText(R.id.end, sr.format_time(end));

        return new NotificationCompat.Builder(this)
            .setContent(rv)
            .setOngoing(true)
            .setSmallIcon(R.drawable.notification_icon, h.num)
            .setContentIntent(PendingIntent.getActivity(this, 0,
                                                        new Intent(this, JTTMainActivity.class), 0))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setChannelId(CHANNEL_ID)
            .getNotification();
    }

    public void onStringResourcesChanged(final int changes) {
        show();
    }
}
