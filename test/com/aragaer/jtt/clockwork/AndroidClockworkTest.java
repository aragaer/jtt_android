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
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.aragaer.jtt.core.TickCallback;
import com.aragaer.jtt.core.TransitionProvider;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class AndroidClockworkTest {

	private AndroidClockwork clockwork;

	@Before
	public void setup() {
		clockwork = new AndroidClockwork(Robolectric.application);
	}
	static class TestReceiver extends BroadcastReceiver {
		int wrapped = -1;
		public void onReceive(Context context, Intent intent) {
			if (!intent.getAction().equals(AndroidClockwork.ACTION_JTT_TICK))
				return;
			wrapped = intent.getIntExtra("jtt", 0);
		}
	}

	@Test
	public void shouldSendBroadcast() {
		TransitionProvider transitionProvider = new FakeTransitionProvider();
		transitionProvider.onCreate();
		ShadowContentResolver.registerProvider(TransitionProvider.AUTHORITY, transitionProvider);
		ContentValues location = new ContentValues();
		location.put("lat", 0);
		location.put("lon", 0);
		transitionProvider.update(TransitionProvider.LOCATION, location, null, null);
		TestReceiver receiver = new TestReceiver();
		Robolectric.application.registerReceiver(receiver, new IntentFilter(AndroidClockwork.ACTION_JTT_TICK));
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
