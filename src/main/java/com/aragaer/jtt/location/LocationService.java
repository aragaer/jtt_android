package com.aragaer.jtt.location;
// vim: et ts=4 sts=4 sw=4


public class LocationService {
    private final LocationProvider provider;
    private final LocationChangeNotifier changeNotifier;
    private LocationClient client;

    public LocationService(LocationProvider provider) {
        this.provider = provider;
        this.changeNotifier = null;
    }

    public Location getCurrentLocation() {
        return provider.getCurrentLocation();
    }

    public void registerClient(LocationClient newClient) {
        client = newClient;
        client.onLocationChanged(getCurrentLocation());
    }

    public void locationChanged() {
        if (client != null)
            client.onLocationChanged(getCurrentLocation());
    }
}
