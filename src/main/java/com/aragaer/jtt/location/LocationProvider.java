package com.aragaer.jtt.location;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.clockwork.Astrolabe;


public abstract class LocationProvider {

    protected Astrolabe astrolabe;

    public LocationProvider(Astrolabe astrolabe) {
        this.astrolabe = astrolabe;
    }

    public void postInit() {
        astrolabe.onLocationChanged(getCurrentLocation());
    }

    public abstract Location getCurrentLocation();

}
