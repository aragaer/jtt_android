package com.aragaer.jtt.clockwork;

import java.util.List;

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowAlarmManager.ScheduledAlarm;
import org.robolectric.shadows.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import android.app.AlarmManager;
import android.content.*;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class AndroidMetronomeTest {

	private AndroidMetronome metronome;

	@Before
	public void setup() {
		metronome = new AndroidMetronome(Robolectric.application);
	}

	@Test
	public void shouldScheduleAlarm() {
		metronome.start(0, 100);
		List<ScheduledAlarm> alarms = getScheduledAlarms();
		assertThat(alarms.size(), equalTo(1));
		ScheduledAlarm alarm = alarms.get(0);
		ShadowPendingIntent pending = Robolectric.shadowOf(alarm.operation);
		assertTrue(pending.isServiceIntent());
	}

	@Test
	public void shouldScheduleAlarmWithCorrectStartTime() {
		metronome.start(30, 100);
		List<ScheduledAlarm> alarms = getScheduledAlarms();
		assertThat(alarms.size(), equalTo(1));
		ScheduledAlarm alarm = alarms.get(0);
		assertThat(alarm.triggerAtTime, equalTo(30L));
	}

	@Test
	public void shouldScheduleAlarmWithCorrectInterval() {
		metronome.start(0, 100);
		List<ScheduledAlarm> alarms = getScheduledAlarms();
		assertThat(alarms.size(), equalTo(1));
		ScheduledAlarm alarm = alarms.get(0);
		assertThat(alarm.interval, equalTo(100L));
	}

	@Test
	public void shouldUnscheduleAlarm() {
		metronome.start(0, 100);
		metronome.stop();
		List<ScheduledAlarm> alarms = getScheduledAlarms();
		assertThat(alarms.size(), equalTo(0));
	}

	private List<ScheduledAlarm> getScheduledAlarms() {
		AlarmManager am = (AlarmManager) Robolectric.application
			.getSystemService(Context.ALARM_SERVICE);
		return Robolectric.shadowOf(am).getScheduledAlarms();
	}
}
