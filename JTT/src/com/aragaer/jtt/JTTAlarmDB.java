package com.aragaer.jtt;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class JTTAlarmDB {
    private final static String TAG = JTTAlarmDB.class.getSimpleName();

    private static final String DATABASE_NAME = "JTT";
    private static final String ALARM_TABLE_NAME = "alarms";

    private static final String ALARM_ID_FIELD = "id";
    private static final String ALARM_JTT_FIELD = "jtt";
    private static final String ALARM_TIME_FIELD = "time";
    private static final String ALARM_NAME_FIELD = "name";
    private static final String ALARM_REPEAT_FIELD = "repeat";
    private static final String ALARM_TONE_FIELD = "tone";
    private static final String ALARM_STATE_FIELD = "enabled";
    
    public static final int NO_ID = -1;

    private static String[] columns = { ALARM_JTT_FIELD, ALARM_TIME_FIELD,
            ALARM_NAME_FIELD, ALARM_REPEAT_FIELD, ALARM_TONE_FIELD,
            ALARM_STATE_FIELD };

    private JTTAlarmDBHelper db;
    private JTTHourStringsHelper sh;

    public JTTAlarmDB(Context ctx) {
        db = new JTTAlarmDBHelper(ctx);
        sh = new JTTHourStringsHelper(ctx);

        if (loadAlarm(0) == null)
            saveAlarm(new JTTAlarm(sh.makeHour("Tiger"), "Samurai wake up time"));
    }

    public void saveAlarm(JTTAlarm alarm) {
        ContentValues row = new ContentValues();
        if (alarm.jtt_time == null)
            row.put(ALARM_TIME_FIELD, alarm.time.getTime());
        else
            row.put(ALARM_JTT_FIELD, alarm.jtt_time.num + alarm.jtt_time.fraction);

        row.put(ALARM_NAME_FIELD, alarm.name);
        row.put(ALARM_REPEAT_FIELD, alarm.repeat);
        row.put(ALARM_TONE_FIELD, alarm.tone);
        row.put(ALARM_STATE_FIELD, alarm.disabled ? 0 : 1);

        if (alarm.id == NO_ID) {
            db.getWritableDatabase().insert(ALARM_TABLE_NAME, null, row);
        } else {
            row.put(ALARM_ID_FIELD, alarm.id);
            db.getWritableDatabase().replace(ALARM_TABLE_NAME, null, row);
        }
    }

    public JTTAlarm loadAlarm(int id) {
        Cursor c = db.getReadableDatabase().query(ALARM_TABLE_NAME, columns,
                "id = ?", new String[] { Integer.toString(id) }, null,
                null, null);

        if (c == null) {
            return null;
        } else if (!c.moveToFirst()) {
            c.close();
            return null;
        }

        JTTAlarm res;
        String name = c.getString(2);

        if (c.isNull(0))
            res = new JTTAlarm(new Date(c.getLong(1)), name);
        else
            res = new JTTAlarm(new JTTHour(c.getFloat(0)), name);

        res.repeat = c.getString(3);
        res.tone = c.getString(4);
        res.disabled = c.getInt(5) == 0;

        return res;
    }

    public final class JTTAlarm {
        public int id;
        public JTTHour jtt_time;
        public Date time;
        public String name;
        public String repeat;
        public String tone;
        public Boolean disabled = true;

        public JTTAlarm(JTTHour hour, String n) {
            jtt_time = hour;
            name = n;
        }

        public JTTAlarm(Date t, String n) {
            time = t;
            name = n;
        }
    }

    private final class JTTAlarmDBHelper extends SQLiteOpenHelper {
        private static final int DATABASE_VERSION = 1;
        private static final String ALARM_TABLE_CREATE = "CREATE TABLE "
                + ALARM_TABLE_NAME + " (" + ALARM_ID_FIELD
                + " integer primary key autoincrement, " + ALARM_JTT_FIELD + " float, "
                + ALARM_TIME_FIELD + " long, " + ALARM_NAME_FIELD + " text, "
                + ALARM_REPEAT_FIELD + " text, " + ALARM_STATE_FIELD + " int, "
                + ALARM_TONE_FIELD + " text);";

        public JTTAlarmDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, ALARM_TABLE_CREATE);
            db.execSQL(ALARM_TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
