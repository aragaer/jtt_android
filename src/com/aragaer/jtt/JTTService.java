package com.aragaer.jtt;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
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

	private long sync = 0;
	protected ArrayList<Long> transitions = new ArrayList<Long>();
	private long start_day, end_day;
	private String t_start, t_end;

	public static final int MSG_SETTINGS_CHANGE = 0;
	public static final int MSG_UPDATE_LOCATION = 1;
	public static final int MSG_REGISTER_CLIENT = 2;
	public static final int MSG_UNREGISTER_CLIENT = 3;
	public static final int MSG_HOUR = 4;
	public static final int MSG_STOP = 5;
	public static final int MSG_TRANSITIONS = 6;
	public static final int MSG_INVALIDATE = 7;
	public static final int MSG_SUBTICK = 8;
	public static final int MSG_SYNC = 9;

	private int hour, quarter, part;

	static class JTTHandler extends Handler {
		ArrayList<Messenger> clients = new ArrayList<Messenger>();
		private final WeakReference<JTTService> srv;
		private final WeakReference<JTT> calc;

		public JTTHandler(JTTService s) {
			srv = new WeakReference<JTTService>(s);
			calc = new WeakReference<JTT>(s.calculator);
		}

		@Override
		public void handleMessage(Message msg) {
			JTTService s = srv.get();
			switch (msg.what) {
			case MSG_SETTINGS_CHANGE:
				s.notify = msg.getData().getBoolean("notify", s.notify);
				JTTUtil.changeLocale(s, msg.getData().getString("locale"));
				if (s.notify)
					s.notify_helper(s.hour, s.quarter, s.part);
				else
					s.nm.cancel(APP_ID);
				break;
			case MSG_UPDATE_LOCATION:
				String ll[] = msg.getData().getString("latlon").split(":");
				calc.get().move(Float.parseFloat(ll[0]),
						Float.parseFloat(ll[1]));
				informClients(Message.obtain(null, MSG_INVALIDATE));
				break;
			case MSG_REGISTER_CLIENT:
				try {
					msg.replyTo.send(Message.obtain(null, MSG_HOUR, s.hour, s.part));
					clients.add(msg.replyTo);
				} catch (RemoteException e) {
					Log.w(TAG, "Client registered but failed to get data");
				}
				break;
			case MSG_UNREGISTER_CLIENT:
				clients.remove(msg.replyTo);
				break;
			case MSG_STOP:
				s.force_stop = true;
				s.stopSelf();
				break;
			case MSG_TRANSITIONS:
				try {
					msg.replyTo.send(trans_msg(msg));
				} catch (RemoteException e) {
					Log.w(TAG, "Client requested transitions data but failed to get answer");
				}
				break;
			default:
				super.handleMessage(msg);
				break;
			}
		}

		public void informClients(Message msg) {
			int i = clients.size();
			while (i-- > 0)
				try {
					clients.get(i).send(msg);
				} catch (RemoteException e) {
					/*
					 * The client is dead. Remove it from the list; we are going
					 * through the list from back to front so this is safe to do
					 * inside the loop.
					 */
					clients.remove(i);
				}
		}

		private Message trans_msg(Message rq) {
			Message resp = Message.obtain(null, MSG_TRANSITIONS);
			Bundle b = new Bundle(rq.getData());
			long jdn = b.getLong("jdn");
			long[] tr = calc.get().computeTr(jdn);
			b.putLong("sunrise", tr[0]);
			b.putLong("sunset", tr[1]);
			resp.setData(b);
			return resp;
		}
	};

	final JTTHandler handler = new JTTHandler(this);
	final Messenger messenger = new Messenger(handler);

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

		rv.setTextViewText(R.id.start, t_start);
		rv.setTextViewText(R.id.end, t_end);

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

	private Clockwork clk = new Clockwork(calculator);
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

		if (!notify)
			nm.cancel(APP_ID);

		receiver.register(this);
		clk.set_context(this);
		clk.reset();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Service destroying");

		clk.go_sleep();
		receiver.unregister();

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
