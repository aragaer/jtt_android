// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;


public class Jtt {

    private static JttComponent jttComponent;

    public static JttComponent getJttComponent() {
        if (jttComponent == null)
            jttComponent = DaggerJttComponent.create();
        return jttComponent;
    }
}
