package com.aragaer.jtt.location;
// vim: et ts=4 sts=4 sw=4


public class TestLocationProvider implements LocationProvider {
    private Location nextResult;

    public void setNextResult(Location newNextResult) {
        nextResult = newNextResult;
    }

    public Location getCurrentLocation() {
        return nextResult;
    }
}
