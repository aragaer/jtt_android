package com.aragaer.jtt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

public class JTTService extends Service {
    private JTT calculator;
    private JTTHour hour = new JTTHour(0);
    private Notification notification;
    private NotificationManager nm;
    private static final int APP_ID = 0;
    private PendingIntent pending_main;
    private SharedPreferences settings;
    private static final DateFormat df = new SimpleDateFormat("HH:mm");
    private JTTHourStringsHelper hs;

    private Boolean notify;

    ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    public static final int MSG_TOGGLE_NOTIFY = 0;
    public static final int MSG_UPDATE_LOCATION = 1;
    public static final int MSG_REGISTER_CLIENT = 2;
    public static final int MSG_UNREGISTER_CLIENT = 3;
    public static final int MSG_HOUR = 4;
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    private static final String TAG = JTTService.class.getSimpleName();

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_TOGGLE_NOTIFY:
                notify = msg.getData().getBoolean("notify");
                notify_helper(new Date());
                break;
            case MSG_UPDATE_LOCATION:
                String ll[] = msg.getData().getString("latlon").split(":");
                calculator = new JTT(Float.parseFloat(ll[0]),
                        Float.parseFloat(ll[1]), TimeZone.getDefault());
                doCalc();
                break;
            case MSG_REGISTER_CLIENT:
                try {
                    msg.replyTo.send(Message.obtain(null, MSG_HOUR, hour.num,
                            Math.round(hour.fraction * 100)));
                    mClients.add(msg.replyTo);
                } catch (RemoteException e) {
                    Log.w(TAG, "Client registered but failed to get data");
                }
                break;
            case MSG_UNREGISTER_CLIENT:
                mClients.remove(msg.replyTo);
                break;
            default:
                super.handleMessage(msg);
                break;
            }
        }
    }

    private Timer timer;
    private TimerTask updateTask = new TimerTask() {
        @Override
        public void run() {
            doCalc();
        }
    };

    private void init_notification(Date when) {
        if (nm == null)
            nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notification == null)
            notification = new Notification(R.drawable.notification_icon,
                    getBaseContext().getString(R.string.app_name),
                    when.getTime());

        if (notify)
            notification.flags = Notification.FLAG_ONGOING_EVENT
                    | Notification.FLAG_NO_CLEAR;
        else
            notification.flags = 0;

    }

    private void notify_helper(Date when) {
        final Context ctx = getBaseContext();

        if (notification == null && !notify) // do nothing
            return;

        init_notification(when);

        if (notification.contentView == null)
            notification.contentView = new RemoteViews(getPackageName(),
                    R.layout.notification);
        ;
        if (notification.contentIntent == null)
            notification.contentIntent = pending_main;

        notification.contentView.setTextViewText(R.id.image,
                JTTHour.Glyphs[hour.num]);
        notification.contentView.setTextViewText(R.id.title,
                hs.getHrOf(hour.num));
        notification.contentView.setTextViewText(R.id.text,
                Math.round(hour.fraction * 100) + "%");
        notification.contentView.setTextViewText(R.id.when, df.format(when));

        notification.iconLevel = hour.num;
        nm.notify(APP_ID, notification);
    }

    private void doCalc() {
        Date when = new Date();
        hour = calculator.time_to_jtt(when);
        notify_helper(when);
        Message msg = Message.obtain(null, MSG_HOUR, hour.num,
                Math.round(hour.fraction * 100));

        for (int i = mClients.size() - 1; i >= 0; i--)
            try {
                mClients.get(i).send(msg);
            } catch (RemoteException e) {
                /*
                 * The client is dead. Remove it from the list; we are going
                 * through the list from back to front so this is safe to do
                 * inside the loop.
                 */
                mClients.remove(i);
            }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onStart(Intent intent, int startid) {
        Log.i(TAG, "Service starting");
        hs = new JTTHourStringsHelper(this);
        settings = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        String[] ll = settings.getString("jtt_loc", "0.0:0.0").split(":");

        calculator = new JTT(Float.parseFloat(ll[0]), Float.parseFloat(ll[1]),
                TimeZone.getDefault());
        hour = calculator.time_to_jtt(new Date());
        Log.d(TAG, "rate = " + calculator.rate);
        Log.d(TAG, "Next hour at " + calculator.nextHour.toLocaleString());

        Intent JTTMain = new Intent(getBaseContext(), JTTMainActivity.class);
        pending_main = PendingIntent.getActivity(this, 0, JTTMain, 0);
        notify = settings.getBoolean("jtt_notify", true);

        timer = new Timer("JTTServiceTimer");
        try {
            timer.scheduleAtFixedRate(updateTask, 0, 60 * 1000L);
        } catch (IllegalStateException e) {
            Log.i(TAG, "Timer is already running");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroying");

        timer.cancel();
        timer = null;

        if (settings.getBoolean("jtt_bootup", true)) {
            init_notification(new Date());

            notification.setLatestEventInfo(JTTService.this, getBaseContext()
                    .getString(R.string.srv_fail),
                    getBaseContext().getString(R.string.srv_fail_ex),
                    pending_main);
            notification.when = System.currentTimeMillis();
            notification.iconLevel = hour.num;
            nm.notify(APP_ID, notification);
        }
    }
}
