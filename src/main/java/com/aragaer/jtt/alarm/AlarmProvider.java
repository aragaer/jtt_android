package com.aragaer.jtt.alarm;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class AlarmProvider extends ContentProvider {
	public static final String AUTHORITY = "com.aragaer.jtt.provider.alarm";
	public static final Uri ALARM_URI = Uri.parse("content://" + AUTHORITY);
	public static final String TABLE_NAME = "alarms";
	private static final String DATABASE_NAME = "alarms";
	private static final int DATABASE_VERSION = 1;

	private SQLiteDatabase db;

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long id = db.insert(TABLE_NAME, null, values);
		return ContentUris.withAppendedId(ALARM_URI, id);
	}

	@Override
	public boolean onCreate() {
		db = new AlarmSQLiteHelper(getContext(), DATABASE_NAME, null,
				DATABASE_VERSION).getWritableDatabase();
		return db != null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		return db.query(TABLE_NAME, projection, selection, selectionArgs, null,
				null, null);
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return 0;
	}

}
