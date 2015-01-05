package com.aragaer.jtt;
// vim: et ts=4 sts=4 sw=4

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.aragaer.jtt.ChimeListener;
import com.aragaer.jtt.core.JttTime;
import com.aragaer.jtt.resources.RuntimeResources;
import com.aragaer.jtt.resources.StringResources;


public class NotificationService extends Service {
    private static final int APP_ID = 1;
    private static final String EXTRA_NOTIFICATION_TICKS = "jtt_ticks";

    public static class JttTimeListener extends ChimeListener {

        public void onChime(Context context, int ticks) {
            context.startService(new Intent(context, NotificationService.class)
                    .putExtra(EXTRA_NOTIFICATION_TICKS, ticks));
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void fillNotificationView(RemoteViews view, JttTime time) {
        StringResources sr = RuntimeResources.get(this).getInstance(StringResources.class);
        view.setTextViewText(R.id.image, time.hour.glyph);
        view.setTextViewText(R.id.title, sr.getHrOf(time.hour.ordinal()));
        view.setTextViewText(R.id.quarter, sr.getQuarter(time.quarter.ordinal()));
        int hour_ticks = time.quarter.ordinal()*JttTime.TICKS_PER_QUARTER + time.ticks;
        view.setProgressBar(R.id.fraction, JttTime.TICKS_PER_HOUR, hour_ticks, false);
    }

    private Notification constructNotification(JttTime time) {
        RemoteViews view = new RemoteViews(getPackageName(), R.layout.notification);
        fillNotificationView(view, time);
        return new Notification.Builder(this)
            .setContent(view).setOngoing(true).setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.notification_icon, time.hour.ordinal())
            .setContentIntent(PendingIntent.getActivity(
                        this, 0, new Intent(this, MainActivity.class), 0))
            .getNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int ticks = intent.getIntExtra(EXTRA_NOTIFICATION_TICKS, 0);

        startForeground(APP_ID, constructNotification(JttTime.fromTicks(ticks)));

        return START_STICKY;
    }
}
