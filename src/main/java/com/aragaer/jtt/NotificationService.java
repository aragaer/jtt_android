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

import com.aragaer.jtt.clockwork.ChimeListener;
import com.aragaer.jtt.core.JttTime;


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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int ticks = intent.getIntExtra(EXTRA_NOTIFICATION_TICKS, 0);
        RemoteViews view = new RemoteViews(getPackageName(), R.layout.notification);
        JttTime time = JttTime.fromTicks(ticks);

        view.setTextViewText(R.id.image, time.hour.glyph);
        view.setTextViewText(R.id.title, getResources().getStringArray(R.array.hour_of)[time.hour.ordinal()]);
        view.setTextViewText(R.id.quarter, getResources().getStringArray(R.array.quarter)[time.quarter.ordinal()]);
		view.setProgressBar(R.id.fraction, JttTime.TICKS_PER_HOUR, time.ticks, false);

        Notification notification = new Notification.Builder(this)
            .setContent(view).setOngoing(true).setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.notification_icon, time.hour.ordinal())
			.setContentIntent(PendingIntent.getActivity(
					this, 0, new Intent(this, MainActivity.class), 0))
            .getNotification();

        startForeground(APP_ID, notification);

        return START_STICKY;
    }
}
