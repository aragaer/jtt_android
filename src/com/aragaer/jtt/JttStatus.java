package com.aragaer.jtt;

import com.aragaer.jtt.core.Clockwork;
import com.aragaer.jtt.core.Hour;
import com.aragaer.jtt.resources.RuntimeResources;
import com.aragaer.jtt.resources.StringResources;
import com.aragaer.jtt.resources.StringResources.StringResourceChangeListener;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.RemoteViews;

public class JttStatus extends BroadcastReceiver implements StringResourceChangeListener {
	private static final int APP_ID = 0,
			flags_ongoing = Notification.FLAG_ONGOING_EVENT	| Notification.FLAG_NO_CLEAR;

	private final Context context;
	private final StringResources sr;
	private final Hour h = new Hour(0);
	private long start, end;
	private final NotificationManager nm;

	public JttStatus(final Context ctx) {
		context = ctx;
		nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		sr = RuntimeResources.get(context).getInstance(StringResources.class);
		sr.registerStringResourceChangeListener(this,
			StringResources.TYPE_HOUR_NAME | StringResources.TYPE_TIME_FORMAT);

		context.registerReceiver(this, new IntentFilter(Clockwork.ACTION_JTT_TICK));
	}

	public void release() {
		nm.cancel(APP_ID);
		sr.unregisterStringResourceChangeListener(this);
		context.unregisterReceiver(this);
	}

	@Override
	public void onReceive(Context ctx, Intent intent) {
		final String action = intent.getAction();
		if (!action.equals(Clockwork.ACTION_JTT_TICK))
			return;

		final int wrapped = intent.getIntExtra("jtt", 0);
		Hour.fromWrapped(wrapped, h);

		final long tr[] = intent.getLongArrayExtra("tr");
		start = Hour.getHourBoundary(tr[1], tr[2], h.num % Hour.HOURS);
		end = Hour.getHourBoundary(tr[1], tr[2], h.num % Hour.HOURS + 1);

		show();
	}

	private void show() {
		final Notification n = new Notification(R.drawable.notification_icon,
				context.getString(R.string.app_name), System.currentTimeMillis());
		final RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.notification);

		n.flags = flags_ongoing;
		n.iconLevel = h.num;
		final int hf = h.quarter * Hour.QUARTER_PARTS + h.quarter_parts;

		rv.setTextViewText(R.id.image, Hour.Glyphs[h.num]);
		rv.setTextViewText(R.id.title, sr.getHrOf(h.num));
		rv.setTextViewText(R.id.quarter, sr.getQuarter(h.quarter));
		rv.setProgressBar(R.id.fraction, Hour.HOUR_PARTS, hf, false);
		rv.setProgressBar(R.id.fraction, Hour.HOUR_PARTS, hf, false);
		rv.setTextViewText(R.id.start, sr.format_time(start));
		rv.setTextViewText(R.id.end, sr.format_time(end));

		n.contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, JTTMainActivity.class), 0);
		n.contentView = rv;

		nm.notify(APP_ID, n);
	}

	public void onStringResourcesChanged(final int changes) {
		show();
	}
}
