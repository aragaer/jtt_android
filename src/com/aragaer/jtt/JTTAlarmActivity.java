package com.aragaer.jtt;

import java.text.DateFormat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class JTTAlarmActivity extends Activity {
    private final static String TAG = JTTAlarmActivity.class.getSimpleName();
    protected JTTUtil.ConnHelper conn = new JTTUtil.ConnHelper(this, new IncomingHandler(this));
    private TextView time, glyph, fraction;
    private JTTAlarm alarm = null;

    static class IncomingHandler extends Handler {
    	DateFormat df;
    	JTTAlarmActivity activity;
        public IncomingHandler(JTTAlarmActivity activity) {
        	this.activity = activity;
        	df = android.text.format.DateFormat.getTimeFormat(activity);
		}
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case JTTService.MSG_HOUR:
                activity.glyph.setText(JTTHour.Glyphs[msg.arg1]);
                activity.fraction.setText(msg.arg2 + "%");
                activity.time.setText(df.format(System.currentTimeMillis()));
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

        Bundle b = getIntent().getExtras();
        if (b != null)
            alarm = JTTAlarm.load(this, b.getLong("alarm_id"));
        if (alarm == null)
            alarm = new JTTAlarm(new JTTHour(0),
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

        conn.bind(service, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        conn.release();

        Log.i(TAG, "Activity destroyed");
    }
}