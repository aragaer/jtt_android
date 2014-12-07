package com.aragaer.jtt.core;

import android.content.*;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

public class TransitionProvider extends ContentProvider {
	public static final String AUTHORITY = "com.aragaer.jtt.provider.calculator";
	public static final Uri TRANSITIONS = Uri.parse("content://" + AUTHORITY
			+ "/transitions"), LOCATION = Uri.parse("content://" + AUTHORITY
			+ "/location");

	private static final UriMatcher matcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	private static final int TR = 1, LOC = 2;

	static {
		matcher.addURI(AUTHORITY, "transitions/#", TR);
		matcher.addURI(AUTHORITY, "location", LOC);
	}

	TransitionCalculator calculator;

	@Override
	public boolean onCreate() {
		calculator = new TransitionCalculator();
		return true;
	}

	private static final String PROJECTION_TR[] = { "prev", "start", "end",
			"next", "is_day" };

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		final int action = matcher.match(uri);
		if (action != TR)
			throw new IllegalArgumentException("Unsupported uri for query: "
					+ uri);
		final long now = ContentUris.parseId(uri);
		ThreeIntervals result = calculator.calculateTransitions(now);

		final MatrixCursor c = new MatrixCursor(PROJECTION_TR, 1);
		c.addRow(new Object[] { result.getPreviousStart(), result.getCurrentStart(),
				result.getCurrentEnd(), result.getNextEnd(),
				result.isDayCurrently() ? 1 : 0 });
		return c;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	public String getType(Uri uri) {
		return null;
	}

	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		if (matcher.match(uri) != LOC)
			throw new IllegalArgumentException("Unsupported uri for update: "
					+ uri);
		float latitude = values.getAsFloat("lat");
		float longitude = values.getAsFloat("lon");
		calculator.setLocation(latitude, longitude);
		Log.d("PROVIDER", "Location updated");
		return 0;
	}

	/*
	 * Calculate a total of 4 transitions, tr[1] <= now < tr[2]. Return true if
	 * it is day now
	 */
	public static boolean getSurroundingTransitions(final Context context,
			final long now, final long tr[]) {
		final Cursor c = context.getContentResolver().query(
				ContentUris.withAppendedId(TRANSITIONS, now), null, null, null,
				null);
		c.moveToFirst();
		for (int i = 0; i < 4; i++)
			tr[i] = c.getLong(i);
		final boolean is_day = c.getInt(4) == 1;
		c.close();
		return is_day;
	}

	public static ThreeIntervals getSurroundingTransitions(Context context,
			long now) {
		long[] tr = new long[4];
		boolean is_day = getSurroundingTransitions(context, now, tr);
		return new ThreeIntervals(tr, is_day);
	}

	public static ThreeIntervals getSurroundingTransitions(Context context) {
		return getSurroundingTransitions(context, System.currentTimeMillis());
	}
}
