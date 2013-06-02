package com.aragaer.jtt.core;

import java.util.Calendar;
import java.util.TimeZone;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

public class Calculator extends ContentProvider {
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

	private SunriseSunsetCalculator calculator;

	@Override
	public boolean onCreate() {
		return true;
	}

	private static final String PROJECTION_TR[] = { "start", "end", "is_sunrise" };

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		final int action = matcher.match(uri);
		if (action != TR)
			throw new IllegalArgumentException("Unsupported uri for query: " + uri);
		if (calculator == null)
			throw new IllegalStateException("Location not set");
		final long now = ContentUris.parseId(uri);
		final Calendar date = Calendar.getInstance();
		date.setTimeInMillis(now);

		long prev = calculator.getOfficialSunriseForDate(date);
		long next = calculator.getOfficialSunsetForDate(date);
		boolean is_day = true;

		// if tr2 is before now
		while (now >= prev) {
			prev = next;
			if (is_day) {
				date.add(Calendar.DATE, 1);
				next = calculator.getOfficialSunriseForDate(date);
			} else
				next = calculator.getOfficialSunsetForDate(date);
			is_day = !is_day;
		}

		// (else) if tr1 is after now
		while (now < prev) {
			next = prev;
			if (is_day) {
				date.add(Calendar.DATE, -1);
				prev = calculator.getOfficialSunsetForDate(date);
			} else
				prev = calculator.getOfficialSunriseForDate(date);
			is_day = !is_day;
		}

		final MatrixCursor c = new MatrixCursor(PROJECTION_TR, 1);
		c.addRow(new Object[] {prev, next, is_day ? 1 : 0});
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
		calculator = new SunriseSunsetCalculator(
				new Location(
					values.getAsFloat("lat"),
					values.getAsFloat("lon")),
				TimeZone.getDefault());
		Log.d("PROVIDER", "Location updated");
		return 0;
	}

	/* Put surrounding transitions into tr, return true if it is day now */
	public static boolean getSurroundingTransitions(final Context context, final long now, final long tr[]) {
		final Cursor c = context.getContentResolver()
				.query(ContentUris.withAppendedId(TRANSITIONS, now), null, null, null, null);
		c.moveToFirst();
		tr[0] = c.getLong(0);
		tr[1] = c.getLong(1);
		final boolean is_day = c.getInt(2) == 1;
		c.close();
		return is_day;
	}
}
