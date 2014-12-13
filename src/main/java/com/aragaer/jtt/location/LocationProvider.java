package com.aragaer.jtt.location;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.clockwork.Astrolabe;


public abstract class LocationProvider {

    private Astrolabe astrolabe;

    public void postInit() {
        astrolabe.onLocationChanged(getCurrentLocation());
    }

    public abstract Location getCurrentLocation();

    public void setAstrolabe(Astrolabe newAstrolabe) {
        astrolabe = newAstrolabe;
    }
}
