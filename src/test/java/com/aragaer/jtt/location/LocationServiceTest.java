package com.aragaer.jtt.location;
// vim: et ts=4 sts=4 sw=4

import org.junit.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


public class LocationServiceTest {
    private TestLocationProvider provider;
    private TestLocationChangeNotifier changeNotifier;
    private LocationService service;

    @Before public void setUp() {
        changeNotifier = new TestLocationChangeNotifier();
        provider = new TestLocationProvider();
        service = new LocationService(provider);
        changeNotifier.setService(service);
    }

    @Test public void shouldProvideCurrentLocation() {
        Location location = new Location(1, 1);
        provider.setNextResult(location);

        assertThat(service.getCurrentLocation(), equalTo(location));
    }

    @Test public void shouldNotifyClient() {
        Location location = new Location(1, 1);
        TestLocationClient client = new TestLocationClient();
        service.registerClient(client);
        provider.setNextResult(location);

        changeNotifier.notifyChange();

        assertThat(client.getLocation(), equalTo(location));
    }

    @Test public void shouldProvideCurrentLocationToRegisteredCustomer() {
        Location location = new Location(1, 1);
        TestLocationClient client = new TestLocationClient();
        provider.setNextResult(location);

        service.registerClient(client);

        assertThat(client.getLocation(), equalTo(location));
    }

    static class TestLocationClient implements LocationClient {
        private Location location;

        public void onLocationChanged(Location newLocation) {
            location = newLocation;
        }

        public Location getLocation() {
            return location;
        }
    }
}
