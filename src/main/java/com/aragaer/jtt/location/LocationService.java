package com.aragaer.jtt.location;
// vim: et ts=4 sts=4 sw=4


public class LocationService {
    private final LocationProvider provider;
    private final LocationChangeNotifier changeNotifier;
    private LocationConsumer consumer;

    public LocationService(LocationProvider provider, LocationChangeNotifier changeNotifier) {
        this.provider = provider;
        this.changeNotifier = changeNotifier;
        changeNotifier.setService(this);
    }

    public Location getCurrentLocation() {
        return provider.getCurrentLocation();
    }

    public void registerConsumer(LocationConsumer newConsumer) {
        consumer = newConsumer;
        consumer.onLocationChanged(getCurrentLocation());
    }

    public void locationChanged() {
        if (consumer != null)
            consumer.onLocationChanged(getCurrentLocation());
    }
}
