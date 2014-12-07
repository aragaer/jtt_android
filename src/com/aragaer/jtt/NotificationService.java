package com.aragaer.jtt;
// vim: et ts=4 sts=4 sw=4

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.aragaer.jtt.clockwork.ChimeListener;
import com.aragaer.jtt.core.JttTime;


public class NotificationService extends Service {
	private static final int APP_ID = 0;
    private static final String EXTRA_NOTIFICATION_TICKS = "jtt_ticks";

    public static class JttTimeListener extends ChimeListener {

        public void onChime(Context context, int ticks) {
            context.startService(new Intent(context, NotificationService.class));
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int ticks = intent.getIntExtra(EXTRA_NOTIFICATION_TICKS, 0);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        RemoteViews view = new RemoteViews(getPackageName(), R.layout.notification);
        JttTime time = JttTime.fromTicks(ticks);

        view.setTextViewText(R.id.image, time.hour.glyph);
        view.setTextViewText(R.id.title, getResources().getStringArray(R.array.hour_of)[time.hour.ordinal()]);
        view.setTextViewText(R.id.quarter, getResources().getStringArray(R.array.quarter)[time.quarter.ordinal()]);

        Notification notification = new Notification.Builder(this)
            .setContent(view).setOngoing(true).setOnlyAlertOnce(true)
            .getNotification();

        startForeground(APP_ID, notification);

        return START_STICKY;
    }
}
