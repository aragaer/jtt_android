package com.aragaer.jtt.core;
// vim: et ts=4 sts=4 sw=4

import java.util.List;

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowAlarmManager.ScheduledAlarm;
import org.robolectric.shadows.*;
import org.robolectric.util.ServiceController;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import android.app.AlarmManager;
import android.content.*;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.aragaer.jtt.JttService;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class TimeDateChangeListenerTest {

    private TransitionProviderProbe transitionProvider;

    @Before
    public void setup() {
        transitionProvider = new TransitionProviderProbe();
        transitionProvider.onCreate();
        ShadowContentResolver.registerProvider(TransitionProvider.AUTHORITY, transitionProvider);
        ServiceController<JttService> controller = Robolectric.buildService(JttService.class);
        controller.attach().create().withIntent(new Intent(Robolectric.application, JttService.class)).startCommand(0, 0);
    }

    @Test
    public void testTimeChangeCallback() {
        int queryCount = transitionProvider.queryCount;
        ShadowApplication shadowApplication = Robolectric.getShadowApplication();
        Intent intent = new Intent(Intent.ACTION_TIME_CHANGED);
        List<BroadcastReceiver> receiversForIntent = shadowApplication.getReceiversForIntent(intent);

        BroadcastReceiver receiver = receiversForIntent.get(0);
        receiver.onReceive(Robolectric.application, intent);

        AlarmManager am = (AlarmManager) Robolectric.application.getSystemService(Context.ALARM_SERVICE);
        List<ScheduledAlarm> alarms = Robolectric.shadowOf(am).getScheduledAlarms();
        assertThat(alarms.size(), equalTo(1));
        assertThat(transitionProvider.queryCount, equalTo(queryCount + 1));
    }

    @Test
    public void testDateChangeCallback() {
        int queryCount = transitionProvider.queryCount;
        ShadowApplication shadowApplication = Robolectric.getShadowApplication();
        Intent intent = new Intent(Intent.ACTION_DATE_CHANGED);
        List<BroadcastReceiver> receiversForIntent = shadowApplication.getReceiversForIntent(intent);

        BroadcastReceiver receiver = receiversForIntent.get(0);
        receiver.onReceive(Robolectric.application, intent);

        AlarmManager am = (AlarmManager) Robolectric.application.getSystemService(Context.ALARM_SERVICE);
        List<ScheduledAlarm> alarms = Robolectric.shadowOf(am).getScheduledAlarms();
        assertThat(alarms.size(), equalTo(1));
        assertThat(transitionProvider.queryCount, equalTo(queryCount + 1));
    }

    static class TransitionProviderProbe extends TransitionProvider {
        int queryCount;
        @Override
        public Cursor query(Uri uri, String[] projection, String selection,
                String[] selectionArgs, String sortOrder) {
            queryCount++;
            return super.query(uri, projection, selection, selectionArgs, sortOrder);
        }
    }
}
