// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;


import org.junit.AfterClass;
import org.junit.BeforeClass;

import androidx.test.uiautomator.UiDevice;

import androidx.test.filters.LargeTest;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;


@LargeTest
public class LandscapeTest extends JTTMainActivityTest {

    @BeforeClass public static void rotate() throws Exception {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        device.setOrientationLeft();
    }

    @AfterClass public static void unfreeze() throws Exception {
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        device.unfreezeRotation();
    }
}
