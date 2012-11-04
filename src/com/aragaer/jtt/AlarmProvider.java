package com.aragaer.jtt;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class AlarmProvider extends ContentProvider {
	private final static String TAG = JTTAlarmDB.class.getSimpleName();

	private static final String DATABASE_NAME = "JTT";
	private static final String ALARM_TABLE_NAME = "alarms";

	// public constants for client development
	public static final String AUTHORITY = "com.aragaer.jtt.provider.alarm";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + Alarms.CONTENT_PATH);

	// helper constants for use with the UriMatcher
	private static final int ALARM_LIST = 1;
	private static final int ALARM_ID = 2;
	private static final UriMatcher URI_MATCHER;

	/**
	 * Column and content type definitions for the LentItemsProvider.
	 */
	public static interface Alarms extends BaseColumns {
		public static final Uri CONTENT_URI = AlarmProvider.CONTENT_URI;
		public static final String JTT = "jtt";
		public static final String TIME = "time";
		public static final String NAME = "name";
		public static final String REPEAT = "repeat";
		public static final String TONE = "tone";
		public static final String ENABLED = "enabled";
		public static final String CONTENT_PATH = "alarms";
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
				+ "/vnd.jtt.alarms";
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
				+ "/vnd.jtt.alarms";
		public static final String[] PROJECTION_ALL = { _ID, NAME, JTT, TIME,
				REPEAT, TONE, ENABLED };
	}

	private SQLiteDatabase db = null;

	// prepare the UriMatcher
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, Alarms.CONTENT_PATH, ALARM_LIST);
		URI_MATCHER.addURI(AUTHORITY, Alarms.CONTENT_PATH + "/#", ALARM_ID);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		switch (URI_MATCHER.match(uri)) {
		case ALARM_LIST:
			// all nice and well
			break;
		case ALARM_ID:
			String where = Alarms._ID + " = ?";
			long id = ContentUris.parseId(uri);
			if (TextUtils.isEmpty(selection)) {
				selection = "";
				selectionArgs = new String[1];
			} else {
				String new_args[] = new String[selectionArgs.length + 1];
				System.arraycopy(selectionArgs, 0, new_args, 0,
						selectionArgs.length);
				selectionArgs = new_args;
			}
			selection += where;
			selectionArgs[selectionArgs.length] = Long.toString(id);
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		int count = db.delete(ALARM_TABLE_NAME, selection, selectionArgs);
		// notify all listeners of changes:
		if (count > 0)
			getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
		case ALARM_LIST:
			return Alarms.CONTENT_TYPE;
		case ALARM_ID:
			return Alarms.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (URI_MATCHER.match(uri) != ALARM_LIST)
			throw new IllegalArgumentException(
					"Unsupported URI for insertion: " + uri);
		long id = db.insert(ALARM_TABLE_NAME, null, values);
		if (id > 0) {
			// notify all listeners of changes and return itemUri:
			Uri itemUri = ContentUris.withAppendedId(uri, id);
			getContext().getContentResolver().notifyChange(itemUri, null);
			return itemUri;
		}
		// s.th. went wrong:
		throw new SQLException("Problem while inserting into "
				+ ALARM_TABLE_NAME + ", uri: " + uri); // use another exception here!!!
	}

	@Override
	public boolean onCreate() {
		db = new JTTAlarmDB(this.getContext()).getWritableDatabase();
		if (db == null)
			return false;
		if (db.isReadOnly()) {
			db.close();
			db = null;
			return false;
		}
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(ALARM_TABLE_NAME);
		if (TextUtils.isEmpty(sortOrder))
			sortOrder = null;
		switch (URI_MATCHER.match(uri)) {
		case ALARM_LIST:
			// all nice and well
			break;
		case ALARM_ID:
			// limit query to one row at most:
			builder.appendWhere(Alarms._ID + " = " + ContentUris.parseId(uri));
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		Cursor cursor = builder.query(db, projection, selection, selectionArgs,
				null, null, sortOrder);
		// if we want to be notified of any changes:
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		switch (URI_MATCHER.match(uri)) {
		case ALARM_LIST:
			// all nice and well
			break;
		case ALARM_ID:
			String where = Alarms._ID + " = ?";
			long id = ContentUris.parseId(uri);
			if (TextUtils.isEmpty(selection)) {
				selection = "";
				selectionArgs = new String[1];
			} else {
				String new_args[] = new String[selectionArgs.length + 1];
				System.arraycopy(selectionArgs, 0, new_args, 0,
						selectionArgs.length);
				selectionArgs = new_args;
			}
			selection += where;
			selectionArgs[selectionArgs.length] = Long.toString(id);
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		int count = db.update(ALARM_TABLE_NAME, values, selection,
				selectionArgs);
		// notify all listeners of changes:
		if (count > 0)
			getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	private final class JTTAlarmDB extends SQLiteOpenHelper {
		private static final int DATABASE_VERSION = 1;
		private static final String ALARM_TABLE_CREATE = "CREATE TABLE "
				+ ALARM_TABLE_NAME + " (" + Alarms._ID
				+ " integer primary key autoincrement, " + Alarms.JTT
				+ " int, " + Alarms.TIME + " long, " + Alarms.NAME
				+ " text, " + Alarms.REPEAT + " text, " + Alarms.ENABLED
				+ " int, " + Alarms.TONE + " text);";

		public JTTAlarmDB(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, ALARM_TABLE_CREATE);
			db.execSQL(ALARM_TABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}
}
