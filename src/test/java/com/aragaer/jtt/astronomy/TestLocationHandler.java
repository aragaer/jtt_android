// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.astronomy;


public class TestLocationHandler implements LocationHandler {

    private float _latitude, _longitude;

    @Override public void setLocation(float latitude, float longitude) {
        _latitude = latitude;
        _longitude = longitude;
    }

    @Override public float[] getLocation() {
        return new float[] {_latitude, _longitude};
    }
}
