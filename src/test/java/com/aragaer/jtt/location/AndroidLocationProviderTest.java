package com.aragaer.jtt.location;
// vim: et ts=4 sts=4 sw=4

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowPreferenceManager;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.aragaer.jtt.Settings;

import android.content.SharedPreferences;


@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class AndroidLocationProviderTest {

    private LocationProvider provider;

    @Before
    public void setUp() {
        provider = new AndroidLocationProvider(Robolectric.application);
    }

    @Test
    public void shouldRetrieveLocation() {
        SharedPreferences sharedPreferences = ShadowPreferenceManager.getDefaultSharedPreferences(Robolectric.application.getApplicationContext());
        sharedPreferences.edit().putString(Settings.PREF_LOCATION, "1.2:3.4").commit();

        Location location = provider.getCurrentLocation();

        assertEquals(location.getLatitude(), 1.2, 0.0001);
        assertEquals(location.getLongitude(), 3.4, 0.0001);
    }
}