package com.aragaer.jtt;
// vim: et ts=4 sts=4 sw=4

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.aragaer.jtt.clockwork.ChimeListener;
import com.aragaer.jtt.core.JttTime;


public class NotificationService extends ChimeListener {
	private static final int APP_ID = 0;

    public void onChime(Context context, int ticks) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.notification);
        JttTime time = JttTime.fromTicks(ticks);

		view.setTextViewText(R.id.image, time.hour.glyph);
		view.setTextViewText(R.id.title, context.getResources().getStringArray(R.array.hour_of)[time.hour.ordinal()]);
		view.setTextViewText(R.id.quarter, context.getResources().getStringArray(R.array.quarter)[time.quarter.ordinal()]);

        Notification notification = new Notification.Builder(context).setContent(view).setOngoing(true).getNotification();

        notificationManager.notify(APP_ID, notification);
    }

}
