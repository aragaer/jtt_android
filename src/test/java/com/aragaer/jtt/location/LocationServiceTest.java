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
        service = new LocationService(provider, changeNotifier);
    }

    @Test public void shouldProvideCurrentLocation() {
        Location location = new Location(1, 1);
        provider.setNextResult(location);

        assertThat(service.getCurrentLocation(), equalTo(location));
    }

    @Test public void shouldNotifyCustomer() {
        Location location = new Location(1, 1);
        TestLocationConsumer consumer = new TestLocationConsumer();
        service.registerConsumer(consumer);
        provider.setNextResult(location);

        changeNotifier.notifyChange();

        assertThat(consumer.getLocation(), equalTo(location));
    }

    @Test public void shouldProvideCurrentLocationToRegisteredCustomer() {
        Location location = new Location(1, 1);
        TestLocationConsumer consumer = new TestLocationConsumer();
        provider.setNextResult(location);

        service.registerConsumer(consumer);

        assertThat(consumer.getLocation(), equalTo(location));
    }

    static class TestLocationConsumer implements LocationConsumer {
        private Location location;

        public void onLocationChanged(Location newLocation) {
            location = newLocation;
        }

        public Location getLocation() {
            return location;
        }
    }
}
