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
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

public class JttStatus extends BroadcastReceiver implements StringResourceChangeListener {
	private static final int APP_ID = 0;

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
		final int lower = Hour.lowerBoundary(h.num),
			upper = Hour.upperBoundary(h.num);
		start = Hour.getHourBoundary(tr[1], tr[2], lower);
		end = Hour.getHourBoundary(tr[1], tr[2], upper);
		if (end < start) {// Cock or Hare
			if (h.quarter >= 2) // we've passed the transition
				start = Hour.getHourBoundary(tr[0], tr[1], lower);
			else
				end = Hour.getHourBoundary(tr[2], tr[3], upper);
		}

		show();
	}

	private void show() {
		final int hf = h.quarter * Hour.QUARTER_PARTS + h.quarter_parts;
		final RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.notification);

		rv.setTextViewText(R.id.image, Hour.Glyphs[h.num]);
		rv.setTextViewText(R.id.title, sr.getHrOf(h.num));
		rv.setTextViewText(R.id.quarter, sr.getQuarter(h.quarter));
		rv.setProgressBar(R.id.fraction, Hour.HOUR_PARTS, hf, false);
		rv.setProgressBar(R.id.fraction, Hour.HOUR_PARTS, hf, false);
		rv.setTextViewText(R.id.start, sr.format_time(start));
		rv.setTextViewText(R.id.end, sr.format_time(end));

		final Notification n = new NotificationCompat.Builder(context)
			.setContent(rv)
			.setOngoing(true)
			.setSmallIcon(R.drawable.notification_icon, h.num)
			.setContentIntent(PendingIntent.getActivity(
					context, 0, new Intent(context, MainActivity.class), 0))
			.build();

		nm.notify(APP_ID, n);
	}

	public void onStringResourcesChanged(final int changes) {
		show();
	}
}
