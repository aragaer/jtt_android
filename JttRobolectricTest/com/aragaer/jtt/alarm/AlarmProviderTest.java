package com.aragaer.jtt.alarm;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static com.aragaer.jtt.alarm.AlarmProvider.ALARM_URI;

@RunWith(RobolectricTestRunner.class)
public class AlarmProviderTest {
	private ContentProvider provider;

	@Before
	public void setUp() {
		provider = new AlarmProvider();
		assertTrue(provider.onCreate());
	}

	@Test
	public void shouldStartEmpty() {
		Cursor result = provider.query(ALARM_URI, null, null, null, null);
		assertNotNull(result);
		assertThat(result.getCount(), equalTo(0));
	}

	@Test
	public void shouldStoreAlarms() {
		ContentValues alarms = new ContentValues();
		alarms.put(Alarm.JTT, 0);
		Uri response = provider.insert(ALARM_URI, alarms);
		assertThat(response.getLastPathSegment(), equalTo("1"));
	}

	@Test
	public void shouldReturnStoredAlarms() {
		ContentValues alarms = new ContentValues();
		alarms.put(Alarm.JTT, 0);
		provider.insert(ALARM_URI, alarms);
		Cursor result = provider.query(ALARM_URI, null, null, null, null);
		assertThat(result.getCount(), equalTo(1));
		result.moveToFirst();
		Alarm alarm = Alarm.fromCursor(result);
		assertEquals(alarm.jtt, 0);
	}
}
