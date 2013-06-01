package com.aragaer.jtt;

import com.aragaer.jtt.resources.RuntimeResources;
import com.aragaer.jtt.resources.StringResources;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class JttStatus {
	private static final int APP_ID = 0,
			flags_ongoing = Notification.FLAG_ONGOING_EVENT	| Notification.FLAG_NO_CLEAR;

	public static void notify(final Context ctx, final int hn, final int hf, final long start, final long end) {
		final Notification n = new Notification(R.drawable.notification_icon,
				ctx.getString(R.string.app_name), System.currentTimeMillis());
		final RemoteViews rv = new RemoteViews(ctx.getPackageName(), R.layout.notification);
		final StringResources sr = RuntimeResources.get(ctx).getInstance(StringResources.class);

		n.flags = flags_ongoing;
		n.iconLevel = hn;

		rv.setTextViewText(R.id.image, JTTHour.Glyphs[hn]);
		rv.setTextViewText(R.id.title, sr.getHrOf(hn));
		rv.setTextViewText(R.id.percent, String.format("%d%%", hf));
		rv.setProgressBar(R.id.fraction, 100, hf, false);
		rv.setTextViewText(R.id.start, sr.format_time(start));
		rv.setTextViewText(R.id.end, sr.format_time(end));

		n.contentIntent = PendingIntent.getActivity(ctx, 0, new Intent(ctx, JTTMainActivity.class), 0);
		n.contentView = rv;

		((NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE)).notify(APP_ID, n);
	}

	public static void clear(final Context ctx) {
		((NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(APP_ID);
	}
}
