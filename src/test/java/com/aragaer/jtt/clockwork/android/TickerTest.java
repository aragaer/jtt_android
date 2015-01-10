package com.aragaer.jtt.clockwork.android;
// vim: et ts=4 sts=4 sw=4

import java.util.List;

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.*;
import org.robolectric.shadows.ShadowAlarmManager.ScheduledAlarm;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import android.app.AlarmManager;
import android.content.*;

import com.aragaer.jtt.clockwork.TickCounter;


@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class TickerTest {

    private ClockTickCallback callback;
    private TestTickCounter counter;

    @Before public void setUp() {
        counter = new TestTickCounter();
        callback = new ClockTickCallback(counter, 0, 1);
        Ticker.setCallback(callback);
    }

    @After public void tearDown() {
        Ticker.setCallback(null);
    }

    @Test(expected=IllegalStateException.class)
    public void shouldRequireCallback() {
        Ticker.setCallback(null);
        Ticker.start(Robolectric.application, 0, 20);
    }

	@Test public void shouldScheduleAlarmForSelf() {
        Ticker.start(Robolectric.application, 0, 20);

		List<ScheduledAlarm> alarms = getScheduledAlarms();
		assertThat(alarms.size(), equalTo(1));

		ScheduledAlarm alarm = alarms.get(0);
		ShadowPendingIntent pending = Robolectric.shadowOf(alarm.operation);
        ShadowIntent intent = Robolectric.shadowOf(pending.getSavedIntent());

		assertTrue(pending.isServiceIntent());
        assertEquals(pending.getSavedContext(), Robolectric.application);
        assertEquals(intent.getIntentClass(), Ticker.class);
	}

	@Test public void shouldScheduleAlarmWithCorrectStartTime() {
        Ticker.start(Robolectric.application, 30, 20);
		List<ScheduledAlarm> alarms = getScheduledAlarms();
		assertThat(alarms.size(), equalTo(1));
		ScheduledAlarm alarm = alarms.get(0);
		assertThat(alarm.triggerAtTime, equalTo(30L));
	}

	@Test public void shouldScheduleAlarmWithCorrectInterval() {
        Ticker.start(Robolectric.application, 0, 20);
		List<ScheduledAlarm> alarms = getScheduledAlarms();
		assertThat(alarms.size(), equalTo(1));
		ScheduledAlarm alarm = alarms.get(0);
		assertThat(alarm.interval, equalTo(20L));
	}

    @Test public void shouldUnscheduleAlarm() {
        Ticker.start(Robolectric.application, 0, 20);
		Ticker.stop(Robolectric.application);
		List<ScheduledAlarm> alarms = getScheduledAlarms();
		assertThat(alarms.size(), equalTo(0));
	}

    @Test public void shouldTriggerCallback() {
        new MockTicker().onHandleIntent(null);
        assertThat(counter.calls, equalTo(1));
    }

    class MockTicker extends Ticker {
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

    static private class TestTickCounter extends TickCounter {
        int calls;
        @Override
        public void set(int ticks) {
            calls++;
        }
    }
}
