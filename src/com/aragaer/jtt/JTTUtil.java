package com.aragaer.jtt;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public final class JTTUtil {
	public static class ConnHelper {
		private Context context;
		private final static String TAG = "conn helper";

		private Messenger remote = null;
		private Messenger local = null;

		public ConnHelper(Context ctx, Handler handler) {
			context = ctx;
			local = new Messenger(handler);
		}

		private ServiceConnection conn = new ServiceConnection() {
			public void onServiceConnected(ComponentName name, IBinder service) {
				remote = new Messenger(service);
				try {
					Message msg = Message.obtain(null,
							JTTService.MSG_REGISTER_CLIENT);
					msg.replyTo = local;
					remote.send(msg);
					Log.i(TAG, "Service connection established");
				} catch (RemoteException e) {
					// In this case the service has crashed before we could even
					// do anything with it
					Log.i(TAG, "Service connection can't be established");
				}
			}

			public void onServiceDisconnected(ComponentName name) {
				remote = null;
				Log.i(TAG, "Service connection closed");
			}
		};

		public void send_msg_to_service(int what, Bundle b) {
			Message msg = Message.obtain(null, what);
			if (b != null)
				msg.setData(b);

			msg.replyTo = local;
			try {
				remote.send(msg);
			} catch (RemoteException e) {
				Log.i(TAG, "Service connection broken");
			} catch (NullPointerException e) {
				Log.i(TAG, "Service not connected");
			}
		}

		public boolean bind(Intent service, int flags) {
			return context.bindService(service, conn, flags);
		}

		public void release() {
			send_msg_to_service(JTTService.MSG_UNREGISTER_CLIENT, null);

			try {
				context.unbindService(conn);
			} catch (Throwable t) {
				Log.w(TAG, "Failed to unbind from the service", t);
			}
			context = null;
		}
	}

	/* sets a value to a text field */
	final static void t(View v, int id, String t) {
		((TextView) v.findViewById(id)).setText(t);
	}

	static final int themes[] = {R.style.JTTTheme, R.style.DarkTheme};
	public static final void setTheme(Context c) {
		String theme = PreferenceManager.getDefaultSharedPreferences(c).getString("jtt_theme", c.getString(R.string.theme_default));
		c.setTheme(themes[Integer.parseInt(theme)]);
	}
}
