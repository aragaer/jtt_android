package com.aragaer.jtt.location;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.clockwork.Astrolabe;


public abstract class LocationProvider {

    public LocationProvider(Astrolabe astrolabe) {
    }

    public abstract Location getCurrentLocation();

}
