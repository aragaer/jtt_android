package com.aragaer.jtt;
// vim: et ts=4 sts=4 sw=4

import java.util.List;

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.*;
import org.robolectric.shadows.ShadowAlarmManager.ScheduledAlarm;
import org.robolectric.util.ServiceController;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import android.app.*;
import android.content.*;

import com.aragaer.jtt.clockwork.AndroidClock;
import com.aragaer.jtt.clockwork.TickService;


@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk=18, shadows={com.aragaer.jtt.test.ShadowNotificationBuilder.class})
public class JttServiceTest {

    @Test
    public void shouldStartTicking() {
        ServiceController<JttService> controller = startService();

        List<ScheduledAlarm> alarms = getScheduledAlarms();
        assertThat(alarms.size(), equalTo(1));

		ScheduledAlarm alarm = alarms.get(0);
		ShadowPendingIntent pending = Robolectric.shadowOf(alarm.operation);
        ShadowIntent intent = Robolectric.shadowOf(pending.getSavedIntent());

		assertTrue(pending.isServiceIntent());
        assertEquals(pending.getSavedContext(), controller.get());
        assertEquals(intent.getIntentClass(), TickService.class);
    }

    @Test
    public void shouldCreateBroadcastForTicks() throws PendingIntent.CanceledException {
        TestReceiver receiver = new TestReceiver();
        Robolectric.application.registerReceiver(receiver, new IntentFilter(AndroidClock.ACTION_JTT_TICK));
        ServiceController<JttService> controller = startService();

        assertThat(receiver.calls, equalTo(0));

        new TickServiceMock().onHandleIntent(null);

        assertThat(receiver.calls, equalTo(1));
    }

    static class TestReceiver extends BroadcastReceiver {
        int wrapped = -1;
        int calls;
        public void onReceive(Context context, Intent intent) {
            if (!intent.getAction().equals(AndroidClock.ACTION_JTT_TICK))
                return;
            calls++;
            wrapped = intent.getIntExtra("jtt", 0);
        }
    }

    private ServiceController<JttService> startService() {
        ServiceController<JttService> controller = Robolectric.buildService(JttService.class);
        controller.attach()
                  .create()
                  .withIntent(new Intent(Robolectric.application, JttService.class))
                  .startCommand(0, 0);
        return controller;
    }

    private List<ScheduledAlarm> getScheduledAlarms() {
        AlarmManager am = (AlarmManager) Robolectric.application
            .getSystemService(Context.ALARM_SERVICE);
        return Robolectric.shadowOf(am).getScheduledAlarms();
    }

    class TickServiceMock extends TickService {
        @Override
        public void onHandleIntent(Intent intent) {
            super.onHandleIntent(intent);
        }
    }
}
