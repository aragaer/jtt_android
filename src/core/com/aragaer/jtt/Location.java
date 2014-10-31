package com.aragaer.jtt;
// vim: et ts=4 sts=4 sw=4


public class Location {
    private final float latitude;
    private final float longitude;

    public Location(float latitude, float longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }
}
