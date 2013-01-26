package com.aragaer.jtt;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

public class JTTService extends Service {
	private static final String TAG = JTTService.class.getSimpleName();
	private final JTT calculator = new JTT(0, 0);
	private NotificationManager nm;
	private static final int flags_ongoing = Notification.FLAG_ONGOING_EVENT
			| Notification.FLAG_NO_CLEAR;
	private static final int APP_ID = 0;

	private PendingIntent pending_main;
	private JTTUtil.StringsHelper hs = null;

	private boolean notify, force_stop = false;
	private static Notification notification;

	private String t_start, t_end;

	private String app_name;
	private static final int bar_ids[] = {R.id.fraction1, R.id.fraction2, R.id.fraction3, R.id.fraction4};
	private void notify_helper(int hn, int hq, int hf) {
		notification = new Notification(R.drawable.notification_icon,
				app_name, System.currentTimeMillis());
		RemoteViews rv = new RemoteViews(getPackageName(),
				R.layout.notification);

		notification.flags = flags_ongoing;
		notification.iconLevel = hn;
		rv.setTextViewText(R.id.image, JTTHour.Glyphs[hn]);
		rv.setTextViewText(R.id.title, hs.getHrOf(hn));
		int i;
		for (i = 0; i < hq; i++)
			rv.setProgressBar(bar_ids[i], 1, 1, false);
		rv.setProgressBar(bar_ids[i], JTTHour.PARTS, hf, false);
		while (++i < JTTHour.QUARTERS)
			rv.setProgressBar(bar_ids[i], 1, 0, false);

//		rv.setTextViewText(R.id.start, t_start);
//		rv.setTextViewText(R.id.end, t_end);

		notification.contentIntent = pending_main;
		notification.contentView = rv;
		nm.notify(APP_ID, notification);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new IJttService.Stub() {
			public long[] getTr(long jdn) throws RemoteException {
				return calculator.computeTr(jdn);
			}
		};
	}

	@Override
	public void onStart(Intent intent, int startid) {
		Log.d(TAG, "Service starting");
		if (hs == null) // first run
		init();
	}

	private Clockwork clk = new Clockwork(calculator, this);
	private void init() {
		Log.d(TAG, "Service initializing");
		JTTUtil.initLocale(this);
		hs = JTTUtil.getStringsHelper(this);
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		String[] ll = settings.getString("jtt_loc", "0.0:0.0").split(":");
		calculator.move(Float.parseFloat(ll[0]), Float.parseFloat(ll[1]));

		Intent JTTMain = new Intent(getBaseContext(), JTTMainActivity.class);
		pending_main = PendingIntent.getActivity(this, 0, JTTMain, 0);
		notify = settings.getBoolean("jtt_notify", true);
		app_name = getString(R.string.app_name);
		nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		if (notify)
			registerReceiver(receiver, JttReceiver.filter);
		else
			nm.cancel(APP_ID);

		clk.start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Service destroying");

		unregisterReceiver(receiver);
		clk.stop();

		if (force_stop)
			nm.cancel(APP_ID);
		else {
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext());
			final boolean boot = settings.getBoolean("jtt_bootup", true);
			if (notify || boot) {
				notification = new Notification(R.drawable.notification_icon,
						app_name, System.currentTimeMillis());

				notification.setLatestEventInfo(JTTService.this, getString(R.string.srv_fail),
						getString(R.string.srv_fail_ex), pending_main);
				notification.flags = boot ? flags_ongoing : 0;
				nm.notify(APP_ID, notification);
			}
		}
	}

	public static class JTTStartupReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			if (PreferenceManager.getDefaultSharedPreferences(context)
					.getBoolean("jtt_bootup", true))
				context.startService(new Intent(context, JTTService.class));
		}
	}

	private final JttReceiver receiver = new JttReceiver() {
		void handle_tick(int n, int q, int f) {
			notify_helper(n, q, f);
		}
	};
}
