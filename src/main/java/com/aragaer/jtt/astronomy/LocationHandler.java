// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.astronomy;


public interface LocationHandler {
    void setLocation(float latitude, float longitude);
    float[] getLocation();
}
