package com.aragaer.jtt.alarm;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.database.sqlite.SQLiteDatabase;

import static org.junit.Assert.assertNotNull;

@RunWith(org.robolectric.RobolectricTestRunner.class)
public class AlarmSQLiteHelperTest {

	private AlarmSQLiteHelper helper;

	@Before
	public void setUp() throws Exception {
		helper = new AlarmSQLiteHelper(Robolectric.application, "path", null, 1);
	}

	@Test
	public void getInitialReadableDatabase() throws Exception {
		SQLiteDatabase database = helper.getReadableDatabase();
		assertNotNull(database);
	}

	@Test
	public void getInitialWritableDatabase() throws Exception {
		SQLiteDatabase database = helper.getWritableDatabase();
		assertNotNull(database);
	}

	@Test
	public void createInitialTable() throws Exception {
		SQLiteDatabase database = helper.getWritableDatabase();
		database.compileStatement("select * from " + AlarmProvider.TABLE_NAME
				+ ";");
	}
}