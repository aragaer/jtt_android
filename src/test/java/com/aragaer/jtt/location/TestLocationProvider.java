package com.aragaer.jtt.location;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.clockwork.Astrolabe;


public class TestLocationProvider extends LocationProvider {

    private static Location nextResult;

    public TestLocationProvider(Astrolabe astrolabe) {
        super(astrolabe);
    }

    public static void setNextResult(Location location) {
        nextResult = location;
    }

    public Location getCurrentLocation() {
        return nextResult;
    }
}
