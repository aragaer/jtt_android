package com.aragaer.jtt.core;

import java.util.List;

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowAlarmManager;
import org.robolectric.shadows.ShadowAlarmManager.ScheduledAlarm;
import org.robolectric.shadows.ShadowContentResolver;
import org.robolectric.shadows.ShadowPendingIntent;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import android.app.AlarmManager;
import android.content.ContentValues;
import android.content.Context;

@RunWith(RobolectricTestRunner.class)
public class ClockworkTest {

	private TransitionProvider transitionProvider;

	@Before
	public void setup() {
		transitionProvider = new TransitionProvider();
		transitionProvider.onCreate();
		ShadowContentResolver.registerProvider(TransitionProvider.AUTHORITY,
				transitionProvider);
		ContentValues location = new ContentValues();
		location.put("lat", 0);
		location.put("lon", 0);
		transitionProvider.update(TransitionProvider.LOCATION, location, null,
				null);
	}

	@Test
	public void shouldAlarmSelf() {
		AlarmManager am = (AlarmManager) Robolectric.application
				.getSystemService(Context.ALARM_SERVICE);
		ShadowAlarmManager shadowAlarmManager = Robolectric.shadowOf(am);
		Clockwork.schedule(Robolectric.application);
		List<ScheduledAlarm> alarms = shadowAlarmManager.getScheduledAlarms();
		assertThat(alarms.size(), equalTo(1));
		ScheduledAlarm alarm = alarms.get(0);
		ShadowPendingIntent pending = Robolectric.shadowOf(alarm.operation);
		assertTrue(pending.isServiceIntent());
	}
}
