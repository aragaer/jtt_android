// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import android.support.test.uiautomator.UiDevice;

import static android.support.test.InstrumentationRegistry.getInstrumentation;


@LargeTest
@RunWith(AndroidJUnit4.class)
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
