package com.aragaer.jtt.location;
// vim: et ts=4 sts=4 sw=4

import dagger.ObjectGraph;

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowPreferenceManager;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.aragaer.jtt.Settings;
import com.aragaer.jtt.clockwork.TestAstrolabe;
import com.aragaer.jtt.clockwork.TestModule;
import com.aragaer.jtt.clockwork.TestClock;

import android.content.SharedPreferences;


@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class AndroidLocationProviderTest {

    private LocationProvider provider;
    private TestAstrolabe astrolabe;

    @Before
    public void setUp() {
        ObjectGraph graph = ObjectGraph.create(new TestModule());
        TestClock clock = graph.get(TestClock.class);
        astrolabe = graph.get(TestAstrolabe.class);
        clock.bindToAstrolabe(astrolabe);
        provider = new AndroidLocationProvider(Robolectric.application);
        provider.setAstrolabe(astrolabe);
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
