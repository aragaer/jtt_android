package com.aragaer.jtt.core;

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
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class ClockworkTest {

	private FakeTransitionProvider transitionProvider;
	private Clockwork clockwork;

	@Before
	public void setup() {
		transitionProvider = new FakeTransitionProvider();
		transitionProvider.onCreate();
		ShadowContentResolver.registerProvider(TransitionProvider.AUTHORITY, transitionProvider);
		ContentValues location = new ContentValues();
		location.put("lat", 0);
		location.put("lon", 0);
		transitionProvider.update(TransitionProvider.LOCATION, location, null, null);
		clockwork = new Clockwork(Robolectric.application);
	}

	@Test
	public void shouldScheduleAlarm() {
		clockwork.schedule();
		List<ScheduledAlarm> alarms = getScheduledAlarms();
		assertThat(alarms.size(), equalTo(1));
		ScheduledAlarm alarm = alarms.get(0);
		ShadowPendingIntent pending = Robolectric.shadowOf(alarm.operation);
		assertTrue(pending.isServiceIntent());
	}

	@Test
	public void shouldScheduleAlarmWithCorrectStartTime() {
		transitionProvider.offset = 30;
		long now = System.currentTimeMillis();
		clockwork.schedule();
		List<ScheduledAlarm> alarms = getScheduledAlarms();
		assertThat(alarms.size(), equalTo(1));
		ScheduledAlarm alarm = alarms.get(0);
		assertThat(alarm.triggerAtTime, equalTo(now - 30));
	}

	@Test
	public void shouldScheduleAlarmWithCorrectInterval() {
		long now = System.currentTimeMillis();
		clockwork.schedule();
		List<ScheduledAlarm> alarms = getScheduledAlarms();
		assertThat(alarms.size(), equalTo(1));
		ScheduledAlarm alarm = alarms.get(0);
		assertThat(alarm.interval, equalTo(transitionProvider.secondIntervalLength / Hour.INTERVAL_TICKS));
	}

	@Test
	public void shouldUnscheduleAlarm() {
		clockwork.schedule();
		clockwork.unschedule();
		List<ScheduledAlarm> alarms = getScheduledAlarms();
		assertThat(alarms.size(), equalTo(0));
	}

	static class TestReceiver extends BroadcastReceiver {
		int wrapped = -1;
		public void onReceive(Context context, Intent intent) {
			if (!intent.getAction().equals(Clockwork.ACTION_JTT_TICK))
				return;
			wrapped = intent.getIntExtra("jtt", 0);
		}
	}

	@Test
	public void shouldSendBroadcast() {
		TestReceiver receiver = new TestReceiver();
		Robolectric.application.registerReceiver(receiver, new IntentFilter(Clockwork.ACTION_JTT_TICK));
		assertThat(receiver.wrapped, equalTo(-1));
		clockwork.onTick();
		assertThat(receiver.wrapped, equalTo(0));
	}

	private List<ScheduledAlarm> getScheduledAlarms() {
		AlarmManager am = (AlarmManager) Robolectric.application
			.getSystemService(Context.ALARM_SERVICE);
		return Robolectric.shadowOf(am).getScheduledAlarms();
	}

	static class FakeTransitionProvider extends TransitionProvider {
		long firstIntervalLength = 20000;
		long secondIntervalLength = 20000;
		long thirdIntervalLength = 20000;
		long offset = 0;
		boolean is_day = false;
		@Override
		public Cursor query(Uri uri, String[] projection, String selection,
				String[] selectionArgs, String sortOrder) {
			long now = ContentUris.parseId(uri);
			MatrixCursor c = new MatrixCursor(new String[] {"prev", "start", "end", "next", "is_day"}, 1);
			c.addRow(new Object[] {
				now - firstIntervalLength - offset,
				now - offset,
				now + secondIntervalLength - offset,
				now + secondIntervalLength + thirdIntervalLength - offset,
				is_day ? 1 : 0
			});
			return c;
		}
	}

}
