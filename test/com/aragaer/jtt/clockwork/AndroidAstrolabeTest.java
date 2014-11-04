package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import android.content.*;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.aragaer.jtt.astronomy.DayInterval;
import com.aragaer.jtt.location.Location;
import com.aragaer.jtt.Settings;
import com.aragaer.jtt.core.TransitionProvider;


@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class AndroidAstrolabeTest {
    private AndroidAstrolabe astrolabe;

    FakeTransitionProvider transitionProvider;

    @Before
    public void setUp() {
        astrolabe = new AndroidAstrolabe(Robolectric.application);
        transitionProvider = new FakeTransitionProvider();
        transitionProvider.onCreate();
        ShadowContentResolver.registerProvider(TransitionProvider.AUTHORITY, transitionProvider);
        ContentValues location = new ContentValues();
        location.put("lat", 0);
        location.put("lon", 0);
        transitionProvider.update(TransitionProvider.LOCATION, location, null, null);
    }

    @Test
    public void shouldReturnCurrentInterval() {
        long now = System.currentTimeMillis();
        DayInterval interval = astrolabe.getCurrentInterval();
        assertThat(interval.getStart(), lessThanOrEqualTo(now));
        assertThat(interval.getEnd(), greaterThan(now));
        assertThat(interval.getLength(), equalTo(transitionProvider.secondIntervalLength));
    }

    @Test
    public void shouldSetLocation() {
        SharedPreferences sharedPreferences = ShadowPreferenceManager.getDefaultSharedPreferences(Robolectric.application.getApplicationContext());
        sharedPreferences.edit().putString(Settings.PREF_LOCATION, "1.2:3.4").commit();
        int count = transitionProvider.locationUpdateCount;
        astrolabe.updateLocation();
        assertThat(transitionProvider.locationUpdateCount, equalTo(count + 1));
        assertEquals(transitionProvider.location.getLatitude(), 1.2, 0.0001);
        assertEquals(transitionProvider.location.getLongitude(), 3.4, 0.0001);
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

        public int locationUpdateCount;
        public Location location;

        @Override
        public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
            locationUpdateCount++;
            location = new Location(values.getAsFloat("lat"), values.getAsFloat("lon"));
            return super.update(uri, values, selection, selectionArgs);
        }
    }
}
