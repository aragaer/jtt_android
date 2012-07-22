package com.aragaer.jtt;

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
import android.widget.RemoteViews;

public class JTTService extends Service {
    private static final String TAG = JTTService.class.getSimpleName();
    private JTT calculator;
    private NotificationManager nm;
    private static final int flags_ongoing = Notification.FLAG_ONGOING_EVENT
            | Notification.FLAG_NO_CLEAR;
    private static final int APP_ID = 0;

    private PendingIntent pending_main;
    private JTTHour.StringsHelper hs;

    private boolean notify, force_stop = false;

    private long sync = 0;
    protected ArrayList<Long> transitions = new ArrayList<Long>();
    private long start_day, end_day;
    private long t_start, t_end;

    ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    // TODO: remove unused stuff
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

    private int hour, sub;

    final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_TOGGLE_NOTIFY:
                notify = msg.getData().getBoolean("notify");
                if (notify)
                    notify_helper(hour, sub);
                else
                    nm.cancel(APP_ID);
                break;
            case MSG_UPDATE_LOCATION:
                String ll[] = msg.getData().getString("latlon").split(":");
                calculator.move(Float.parseFloat(ll[0]),
                        Float.parseFloat(ll[1]));
                reset();
                informClients(Message.obtain(null, MSG_INVALIDATE));
                break;
            case MSG_REGISTER_CLIENT:
                try {
                    msg.replyTo.send(Message.obtain(null, MSG_HOUR, hour,
                            sub));
                    mClients.add(msg.replyTo);
                } catch (RemoteException e) {
                    Log.w(TAG, "Client registered but failed to get data");
                }
                break;
            case MSG_UNREGISTER_CLIENT:
                mClients.remove(msg.replyTo);
                break;
            case MSG_STOP:
                force_stop = true;
                stopSelf();
                break;
            case MSG_TRANSITIONS:
                try {
                    msg.replyTo.send(trans_msg(msg));
                } catch (RemoteException e) {
                    Log.w(TAG, "Client requested transitions data but failed to get answer");
                }
                break;
            case MSG_SYNC:
                wake_up();
                break;
            default:
                super.handleMessage(msg);
                break;
            }
        }
    };

    final Messenger mMessenger = new Messenger(mHandler);

    private Message trans_msg(Message rq) {
        Message resp = Message.obtain(null, MSG_TRANSITIONS);
        Long jdn = rq.getData().getLong("jdn");
        Log.d(TAG, "got request for transitions for day "+jdn);
        Bundle b = new Bundle();
        long[] tr = calculator.computeTr(jdn);
        b.putLong("jdn", jdn);
        b.putLong("sunrise", tr[0]);
        b.putLong("sunset", tr[1]);
        resp.setData(b);
        return resp;
    }

    private String app_name;
    private static final DateFormat df = new SimpleDateFormat("HH:mm");
    private void notify_helper(int hn, int hf) {
        Notification n = new Notification(R.drawable.notification_icon,
                app_name, System.currentTimeMillis());
        RemoteViews rv = new RemoteViews(getPackageName(),
                R.layout.notification);

        n.flags = flags_ongoing;
        n.iconLevel = hn;
        rv.setTextViewText(R.id.image, JTTHour.Glyphs[hn]);
        rv.setTextViewText(R.id.title, hs.getHrOf(hn));
        rv.setTextViewText(R.id.percent, String.format("%d%%", hf));
        rv.setProgressBar(R.id.fraction, 100, hf, false);
        rv.setTextViewText(R.id.start, df.format(t_start));
        rv.setTextViewText(R.id.end, df.format(t_end));

        n.contentIntent = pending_main;
        n.contentView = rv;
        nm.notify(APP_ID, n);
    }

    private void doNotify(int n, int f, int event) {
        if (notify)
            notify_helper(n, f);
        informClients(Message.obtain(null, event, n, f));
    }

    private void informClients(Message msg) {
        int i = mClients.size();
        while (i-- > 0)
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

    @Override
    public void onStart(Intent intent, int startid) {
        Log.i(TAG, "Service starting");
        hs = new JTTHour.StringsHelper(this);
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        String[] ll = settings.getString("jtt_loc", "0.0:0.0").split(":");

        calculator = new JTT(Float.parseFloat(ll[0]), Float.parseFloat(ll[1]));

        Intent JTTMain = new Intent(getBaseContext(), JTTMainActivity.class);
        pending_main = PendingIntent.getActivity(this, 0, JTTMain, 0);
        notify = settings.getBoolean("jtt_notify", true);
        app_name = getString(R.string.app_name);
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (!notify)
            nm.cancel(APP_ID);

        IntentFilter wake = new IntentFilter(Intent.ACTION_SCREEN_ON);
        wake.addAction(Intent.ACTION_TIME_CHANGED);
        wake.addAction(Intent.ACTION_DATE_CHANGED);
        registerReceiver(on, wake);
        registerReceiver(off, new IntentFilter(Intent.ACTION_SCREEN_OFF));

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
                Notification n = new Notification(R.drawable.notification_icon,
                        app_name, System.currentTimeMillis());

                n.setLatestEventInfo(JTTService.this, getString(R.string.srv_fail),
                        getString(R.string.srv_fail_ex), pending_main);
                n.flags = boot ? flags_ongoing : 0;
                nm.notify(APP_ID, n);
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

    void handle_tick(int tick, int sub) {
        doNotify(tick, sub, MSG_HOUR);
    }

    void handle_sub(int tick, int sub) {
        doNotify(tick, sub, MSG_SUBTICK);
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

        /* do not want more than one message being in the system */
        mHandler.removeMessages(MSG_SYNC);
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
        t_start = start + (end - start) * exp_tick / ticks;
        t_end = start + (end - start) * (exp_tick + 1) / ticks;

        if (now - sync > exp_sub * sublen // sync belongs to previous tick interval
                || now < sync) {          // time went backwards!
            hour = exp_tick + isDay * 6;
            sub = exp_sub;
            handle_tick(hour, sub);
        } else if (sub < exp_sub) {
            if (hour % 6 != exp_tick) { // sync should belong to this tick interval
                Log.wtf(TAG, "current tick is "+(hour % 6)+", expected "+exp_tick);
                hour = exp_tick + isDay * 6;
            }
            sub = exp_sub;
            handle_sub(hour, sub);
        }

        sync = System.currentTimeMillis();
        /* doesn't matter if next_sub < sync
         * negative delay is perfectly valid and means that trigger will happen immediately
         */
        mHandler.sendEmptyMessageDelayed(MSG_SYNC, next_sub - sync);
    }

    private final void sleep() {
        mHandler.removeMessages(MSG_SYNC);
    }

    private final void reset() {
        sleep();
        transitions.clear();
        end_day = JTT.longToJDN(System.currentTimeMillis());
        start_day = end_day - 1;
        for (long l : calculator.computeTr(end_day++))
            transitions.add(l);
        wake_up();
    }
}
