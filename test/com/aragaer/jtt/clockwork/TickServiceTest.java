package com.aragaer.jtt.core;
// vim: et ts=4 sts=4 sw=4

import java.util.List;

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowAlarmManager;
import org.robolectric.shadows.ShadowAlarmManager.ScheduledAlarm;
import org.robolectric.shadows.ShadowPendingIntent;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import android.app.AlarmManager;
import android.content.*;


@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class TickServiceTest {

    @After
    public void tearDown() {
        TickService.setCallback(null);
    }

    @Test
    public void shouldSetCallback() {
        TickCallback callback = new TestCallback();
        TickService.setCallback(callback);
    }

    @Test(expected=IllegalStateException.class)
    public void shouldRequireCallback() {
        TickService.start(Robolectric.application, 0, 100, 5);
    }

	@Test
	public void shouldScheduleAlarm() {
        TickService.setCallback(new TestCallback());
        TickService.start(Robolectric.application, 0, 100, 5);
		List<ScheduledAlarm> alarms = getScheduledAlarms();
		assertThat(alarms.size(), equalTo(1));
		ScheduledAlarm alarm = alarms.get(0);
		ShadowPendingIntent pending = Robolectric.shadowOf(alarm.operation);
		assertTrue(pending.isServiceIntent());
	}

	@Test
	public void shouldScheduleAlarmWithCorrectStartTime() {
        TickService.setCallback(new TestCallback());
        TickService.start(Robolectric.application, 30, 100, 5);
		List<ScheduledAlarm> alarms = getScheduledAlarms();
		assertThat(alarms.size(), equalTo(1));
		ScheduledAlarm alarm = alarms.get(0);
		assertThat(alarm.triggerAtTime, equalTo(30L));
	}

	@Test
	public void shouldScheduleAlarmWithCorrectInterval() {
        TickService.setCallback(new TestCallback());
        TickService.start(Robolectric.application, 0, 100, 5);
		List<ScheduledAlarm> alarms = getScheduledAlarms();
		assertThat(alarms.size(), equalTo(1));
		ScheduledAlarm alarm = alarms.get(0);
		assertThat(alarm.interval, equalTo(20L));
	}

    @Test
	public void shouldUnscheduleAlarm() {
        TickService.setCallback(new TestCallback());
        TickService.start(Robolectric.application, 0, 100, 5);
		TickService.stop(Robolectric.application);
		List<ScheduledAlarm> alarms = getScheduledAlarms();
		assertThat(alarms.size(), equalTo(0));
	}

    @Test
    public void shouldTriggerCallback() {
        TickServiceMock mockService = new TickServiceMock();
        TestCallback callback = new TestCallback();
        mockService.setCallback(callback);
        mockService.onHandleIntent(null);
        assertThat(callback.calls, equalTo(1));
    }

    class TickServiceMock extends TickService {
        @Override
        public void onHandleIntent(Intent intent) {
            super.onHandleIntent(intent);
        }
    }

    private List<ScheduledAlarm> getScheduledAlarms() {
		AlarmManager am = (AlarmManager) Robolectric.application
				.getSystemService(Context.ALARM_SERVICE);
		return Robolectric.shadowOf(am).getScheduledAlarms();
    }

    static private class TestCallback implements TickCallback {
        int calls;

        public void onTick(Context context) {
            calls++;
        }
    }
}
