package com.aragaer.jtt.alarm;

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowContentResolver;

import android.content.*;
import android.net.Uri;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.aragaer.jtt.core.Hour;

@RunWith(RobolectricTestRunner.class)
public class AlarmManagerTest {

	private ContentProvider provider;
	private AlarmManager manager;
	private ContentValues savedValues;

	@Before
	public void setup() {
		provider = new AlarmProvider() {
			@Override
			public Uri insert(Uri uri, ContentValues values) {
				savedValues = values;
				return ContentUris.withAppendedId(ALARM_URI, 1);
			}
		};
		provider.onCreate();
		ShadowContentResolver.registerProvider(AlarmProvider.AUTHORITY,
				provider);
		manager = new AlarmManager(Robolectric.application);
	}

	@Test
	public void saveAlarm() throws Exception {
		Hour hour = new Hour(6);
		Alarm alarm = new Alarm(hour);
		manager.saveAlarm(alarm);
		assertNotNull(savedValues);
		assertThat(savedValues.getAsInteger(Alarm.JTT).intValue(), equalTo(hour.wrapped));
	}

	@After
	public void tearDown() {
		savedValues = null;
	}
}
