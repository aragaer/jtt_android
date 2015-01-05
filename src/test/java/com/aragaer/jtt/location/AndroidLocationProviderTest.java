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

import static com.aragaer.jtt.Settings.PREF_LOCATION;

import android.content.SharedPreferences;


@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class AndroidLocationProviderTest {

    private LocationService service;
    private TestLocationClient client;

    @Before public void setUp() {
        client = new TestLocationClient();
        LocationProvider provider = new AndroidLocationProvider(Robolectric.application);
        LocationChangeNotifier changeNotifier = new AndroidLocationChangeNotifier(Robolectric.application);
        service = new LocationService(provider);
        changeNotifier.setService(service);
        service.registerClient(client);
    }

    @Test public void shouldRetrieveLocation() {
        SharedPreferences sharedPreferences = ShadowPreferenceManager
            .getDefaultSharedPreferences(Robolectric.application.getApplicationContext());
        sharedPreferences.edit().putString(PREF_LOCATION, "1.2:3.4").commit();

        Location location = service.getCurrentLocation();

        assertEquals(location.getLatitude(), 1.2, 0.0001);
        assertEquals(location.getLongitude(), 3.4, 0.0001);
    }

    @Test public void shouldReactToLocationChange() {
        SharedPreferences sharedPreferences = ShadowPreferenceManager
            .getDefaultSharedPreferences(Robolectric.application.getApplicationContext());
        sharedPreferences.edit().putString(PREF_LOCATION, "1.2:3.4").commit();

        assertEquals(client.getLocation().getLatitude(), 1.2, 0.0001);
        assertEquals(client.getLocation().getLongitude(), 3.4, 0.0001);
    }

    @Test public void shouldNotReactOnOtherSettings() {
        SharedPreferences sharedPreferences = ShadowPreferenceManager
            .getDefaultSharedPreferences(Robolectric.application.getApplicationContext());
        sharedPreferences.edit().putString("other string", "1.2:3.4").commit();

        assertThat(client.locationChangeCalls, equalTo(1));
    }

    static class TestLocationClient implements LocationClient {
        private Location location;
        public int locationChangeCalls;

        public void onLocationChanged(Location newLocation) {
            location = newLocation;
            locationChangeCalls++;
        }

        public Location getLocation() {
            return location;
        }
    }
}
