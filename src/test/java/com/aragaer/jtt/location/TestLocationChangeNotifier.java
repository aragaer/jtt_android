package com.aragaer.jtt.location;
// vim: et ts=4 sts=4 sw=4


public class TestLocationChangeNotifier implements LocationChangeNotifier {
    private LocationService service;

    public void setService(LocationService service) {
        this.service = service;
    }

    public void notifyChange() {
        if (service != null)
            service.locationChanged();
    }
}
