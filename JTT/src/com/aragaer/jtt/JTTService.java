package com.aragaer.jtt;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

public class JTTService extends Service {
    public final static String TICK_ACTION = "com.aragaer.jtt.ACTION_JTT_TICK";
    private static final String TAG = JTTService.class.getSimpleName();
    private final JTT calculator = new JTT(0, 0);
    private NotificationManager nm;
    private static final int flags_ongoing = Notification.FLAG_ONGOING_EVENT
            | Notification.FLAG_NO_CLEAR;
    private static final int APP_ID = 0;

    private PendingIntent pending_main;
    private JTTHour.StringsHelper hs = null;

    private boolean notify, force_stop = false;
    private static Notification notification;

    private long sync = 0;
    protected ArrayList<Long> transitions = new ArrayList<Long>();
    private long start_day, end_day;
    private String t_start, t_end;

    public static final int MSG_TOGGLE_NOTIFY = 0;
    public static final int MSG_UPDATE_LOCATION = 1;
    public static final int MSG_REGISTER_CLIENT = 2;
    public static final int MSG_UNREGISTER_CLIENT = 3;
    public static final int MSG_HOUR = 4;
    public static final int MSG_STOP = 5;
    public static final int MSG_TRANSITIONS = 6;
    public static final int MSG_INVALIDATE = 7;
    public static final int MSG_SUBTICK = 8;
    public static final int MSG_SYNC = 9;

    private final static Intent TickAction = new Intent();

    private int hour, sub;

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
            case MSG_TOGGLE_NOTIFY:
                s.notify = msg.getData().getBoolean("notify");
                if (s.notify)
                    s.notify_helper(s.hour, s.sub);
                else
                    s.nm.cancel(APP_ID);
                break;
            case MSG_UPDATE_LOCATION:
                String ll[] = msg.getData().getString("latlon").split(":");
                calc.get().move(Float.parseFloat(ll[0]),
                        Float.parseFloat(ll[1]));
                s.reset();
                informClients(Message.obtain(null, MSG_INVALIDATE));
                break;
            case MSG_REGISTER_CLIENT:
                try {
                    msg.replyTo.send(Message.obtain(null, MSG_HOUR, s.hour,
                            s.sub));
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
            case MSG_SYNC:
                s.wake_up();
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
    private static final DateFormat df = new SimpleDateFormat("HH:mm");
    private void notify_helper(int hn, int hf) {
        notification = new Notification(R.drawable.notification_icon,
                app_name, System.currentTimeMillis());
        RemoteViews rv = new RemoteViews(getPackageName(),
                R.layout.notification);

        notification.flags = flags_ongoing;
        notification.iconLevel = hn;
        rv.setTextViewText(R.id.image, JTTHour.Glyphs[hn]);
        rv.setTextColor(R.id.image, notification_text_color);
        rv.setTextViewText(R.id.title, hs.getHrOf(hn));
        rv.setTextColor(R.id.title, notification_text_color);
        rv.setTextViewText(R.id.percent, String.format("%d%%", hf));
        rv.setTextColor(R.id.percent, notification_text_color);
        rv.setProgressBar(R.id.fraction, 100, hf, false);
        rv.setTextViewText(R.id.start, t_start);
        rv.setTextColor(R.id.start, notification_text_color);
        rv.setTextViewText(R.id.end, t_end);
        rv.setTextColor(R.id.end, notification_text_color);

        notification.contentIntent = pending_main;
        notification.contentView = rv;
        nm.notify(APP_ID, notification);
    }
    
    private void doNotify(int n, int f, int event) {
        if (notify)
            notify_helper(n, f);

        TickAction.putExtra("hour", n);
        TickAction.putExtra("fraction", f);

        sendBroadcast(TickAction);

        Message m = Message.obtain(null, event, n, f);
        handler.informClients(m);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    private final BroadcastReceiver on = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            wake_up();
        }
    };
    private final BroadcastReceiver off = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sleep();
        }
    };

    private int notification_text_color;

    /* used to detect notification text color */
    private static final int getNotificationColor(ViewGroup gp) {
        final int count = gp.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = gp.getChildAt(i);
            if (v instanceof TextView) {
                final TextView text = (TextView) v;
                if (TAG.equals(text.getText().toString()))
                    return text.getTextColors().getDefaultColor();
            } else if (v instanceof ViewGroup)
                return getNotificationColor((ViewGroup) v);
        }
        return android.R.color.black;
    }

    @Override
    public void onStart(Intent intent, int startid) {
        Log.d(TAG, "Service starting");
        if (hs == null) // first run
            init();
    }

    private void init() {
        Log.d(TAG, "Service initializing");
        hs = new JTTHour.StringsHelper(this);
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

        try {
            Notification n = new Notification();
            n.setLatestEventInfo(this, TAG, "", null);
            LinearLayout group = new LinearLayout(this);
            notification_text_color = getNotificationColor((ViewGroup) n.contentView.apply(this, group));
            group.removeAllViews();
        } catch (Exception e) {
            notification_text_color = android.R.color.black;
        }

        IntentFilter wake = new IntentFilter(Intent.ACTION_SCREEN_ON);
        wake.addAction(Intent.ACTION_TIME_CHANGED);
        wake.addAction(Intent.ACTION_DATE_CHANGED);
        registerReceiver(on, wake);
        registerReceiver(off, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        TickAction.setAction(TICK_ACTION);

        reset();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroying");

        unregisterReceiver(on);
        unregisterReceiver(off);

        sleep();

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

    private final static int ticks = 6;
    private final static int subs = 100;
    private final static double total = ticks * subs;
    /* This function assumes that we have just woke up
     * Do not attempt to short-cut any calculations based on previous runs
     */
    private void wake_up() {
        long now, start, end;
        int isDay;

        if (notify)
            startForeground(APP_ID, notification);

        /* do not want more than one message being in the system */
        handler.removeMessages(MSG_SYNC);
        while (true) { // we are likely to pass this loop only once
            long[] t = null;
            int len = transitions.size();
            now = System.currentTimeMillis();
            int pos = Collections.binarySearch(transitions, now);
            /*
             * Possible results:
             * -len - 1:        overrun
             * -len - 2 to -2:  OK (tr[-pos - 2] < now < tr[-pos - 1])
             * -1:              underrun
             * 0 to len - 2:    OK (tr[pos] == now < tr[pos + 1])
             * len - 1:         overrun
             *
             * if len == 0, the only result is -1
             * if len == 1, there's no OK results
             */
            if (pos == -1) // right before first element
                t = calculator.computeTr(start_day--);
            if (pos == -len - 1             // after the last element
                    || pos == len - 1)      // equal to the last element
                t = calculator.computeTr(end_day++);
            if (t != null) {
                for (long l : t)
                    transitions.add(l);
                // all this took some time, we should resync
                continue;
            }

            if (pos < 0)
                pos = -pos - 2;

            start = transitions.get(pos);
            end = transitions.get(pos + 1);
            /* cheat - sunrises always have even positions */
            isDay = (pos + 1) % 2;
            break;
        }

        /* we've got start and end */
        long offset = now - start;
        double sublen = ((double) (end - start))/total;
        int exp_total = (int) (offset/sublen);
        int exp_tick = exp_total / subs;
        int exp_sub = exp_total % subs;
        long next_sub = start + Math.round(sublen * (exp_total + 1));

        if (now - sync > exp_sub * sublen // sync belongs to previous tick interval
                || now < sync) {          // time went backwards!
            hour = exp_tick + isDay * 6;
            sub = exp_sub;
            t_start = df.format(start + (end - start) * exp_tick / ticks);
            t_end = df.format(start + (end - start) * (exp_tick + 1) / ticks);
            doNotify(hour, sub, MSG_HOUR);
        } else if (sub < exp_sub) {
            if (hour % 6 != exp_tick) { // sync should belong to this tick interval
                Log.e(TAG, "current tick is "+(hour % 6)+", expected "+exp_tick);
                hour = exp_tick + isDay * 6;
            }
            sub = exp_sub;
            doNotify(hour, sub, MSG_SUBTICK);
        }

        sync = System.currentTimeMillis();
        /* doesn't matter if next_sub < sync
         * negative delay is perfectly valid and means that trigger will happen immediately
         */
        handler.sendEmptyMessageDelayed(MSG_SYNC, next_sub - sync);
    }

    private final void sleep() {
        handler.removeMessages(MSG_SYNC);
    }

    private final void reset() {
        sleep();
        transitions.clear();
        end_day = JTT.longToJDN(System.currentTimeMillis());
        start_day = end_day - 1;
        for (long l : calculator.computeTr(end_day++))
            transitions.add(l);
        sync = 0;
        wake_up();
    }
}
