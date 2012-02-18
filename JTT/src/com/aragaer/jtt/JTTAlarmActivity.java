package com.aragaer.jtt;

import java.util.Date;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class JTTAlarmActivity extends Activity {
    private final static String TAG = JTTAlarmActivity.class.getSimpleName();
    private Messenger mService = null;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private TextView time, glyph, fraction;
    private JTTAlarmDB db;
    private JTTAlarmDB.JTTAlarm alarm;
    private static final DateFormat df = new SimpleDateFormat("HH:mm");

    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            try {
                Message msg = Message.obtain(null,
                        JTTService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
                Log.i(TAG, "Service connection established");
            } catch (RemoteException e) {
                Log.i(TAG, "Service connection can't be established");
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            Log.i(TAG, "Service connection closed");
        }
    };

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case JTTService.MSG_HOUR:
                glyph.setText(JTTHour.Glyphs[msg.arg1]);
                fraction.setText(msg.arg2 + "%");
                time.setText(df.format(new Date()));
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }

    @Override
    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        final Intent service = new Intent(JTTService.class.getName());
        startService(service);

        db = new JTTAlarmDB(this);
        Bundle b = getIntent().getExtras();
        if (b != null)
            alarm = db.loadAlarm(b.getInt("alarm_id"));
        if (alarm == null)
            alarm = db.new JTTAlarm(new JTTHour(0),
                    this.getString(R.string.unknown_alarm));

        setContentView(alarm.time == null ? R.layout.alarm_jtt
                : R.layout.alarm_time);

        OnClickListener close = new OnClickListener() {
            public void onClick(View v) {
                JTTAlarmActivity.this.finish();
            }
        };

        ((Button) findViewById(R.id.stop)).setOnClickListener(close);
        ((Button) findViewById(R.id.snooze)).setOnClickListener(close);

        ((TextView) findViewById(R.id.alarm)).setText(alarm.name);
        time = (TextView) findViewById(R.id.time);
        glyph = (TextView) findViewById(R.id.glyph);
        fraction = (TextView) findViewById(R.id.fraction);

        bindService(service, conn, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            unbindService(conn);
        } catch (Throwable t) {
            Log.w(TAG, "Failed to unbind from the service", t);
        }

        Log.i(TAG, "Activity destroyed");
    }
}
