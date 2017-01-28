// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.android;

import com.aragaer.jtt.core.SscCalculator;
import com.aragaer.jtt.core.ThreeIntervals;

import android.content.*;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

public class SunriseSunsetDataProvider extends ContentProvider {
    public static final String AUTHORITY = "com.aragaer.jtt.provider.calculator";
    public static final Uri TRANSITIONS = Uri.parse("content://" + AUTHORITY + "/transitions"),
	LOCATION = Uri.parse("content://" + AUTHORITY + "/location");

    private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int TR = 1,
	LOC = 2;

    static {
	matcher.addURI(AUTHORITY, "transitions/#", TR);
	matcher.addURI(AUTHORITY, "location", LOC);
    }

    private final SscCalculator _ssc = new SscCalculator();

    @Override
    public boolean onCreate() {
	return true;
    }

    private static final String PROJECTION_TR[] = { "prev", "start", "end", "next", "is_day" };

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
	if (matcher.match(uri) != TR)
	    throw new IllegalArgumentException("Unsupported uri for query: " + uri);
	final long now = ContentUris.parseId(uri);
	ThreeIntervals intervals = _ssc.getSurroundingIntervalsForTimestamp(now);
	final MatrixCursor c = new MatrixCursor(PROJECTION_TR, 1);
	long tr[] = intervals.getTransitions();
	c.addRow(new Object[] {tr[0], tr[1], tr[2], tr[3], intervals.isDay() ? 1 : 0});
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
	    throw new IllegalArgumentException("Unsupported uri for update: " + uri);
	_ssc.setLocation(values.getAsFloat("lat"), values.getAsFloat("lon"));
	Log.d("PROVIDER", "Location updated");
	return 0;
    }

    public static void move(Context context, float latitude, float longitude) {
        final ContentValues location = new ContentValues(2);
        location.put("lat", latitude);
        location.put("lon", longitude);
        context.getContentResolver().update(SunriseSunsetDataProvider.LOCATION, location, null, null);
    }

    public static ThreeIntervals getSurroundingTransitions(final Context context, final long now) {
	final Cursor c = context.getContentResolver()
	    .query(ContentUris.withAppendedId(TRANSITIONS, now), null, null, null, null);
	c.moveToFirst();
	final long[] tr = new long[4];
	for (int i = 0; i < 4; i++)
	    tr[i] = c.getLong(i);
	final boolean is_day = c.getInt(4) == 1;
	c.close();
	return new ThreeIntervals(tr, is_day);
    }
}
