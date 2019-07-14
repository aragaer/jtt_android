// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.astronomy;

import android.content.*;
import android.preference.PreferenceManager;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import org.junit.*;

import com.aragaer.jtt.Settings;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


public class AndroidLocationHandlerTest {

    private LocationHandler locationHandler;
    private Context context;
    private String savedValue;

    @Before public void setUp() {
        context = getInstrumentation().getTargetContext();
        locationHandler = new AndroidLocationHandler(context);
        savedValue = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(Settings.PREF_LOCATION, "");
    }

    @After public void tearDown() {
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putString(Settings.PREF_LOCATION, savedValue)
                .commit();
    }

    @Test public void testSetLocation() {
        locationHandler.setLocation(0f, 0f);
        String location = PreferenceManager
            .getDefaultSharedPreferences(context)
            .getString(Settings.PREF_LOCATION, "");
        assertThat("Should read the correct string",
                   location, equalTo("0.00:0.00"));

    }

    @Test public void testGetLocation() {
        setPreference("1.00:0.00");
        float[] location = locationHandler.getLocation();
        assertThat("Should be same location that was stored",
                   location, equalTo(new float[] { 1f, 0f }));
    }

    @Test public void testSetFromAnother() {
        AndroidLocationHandler another = new AndroidLocationHandler(context);
        another.setLocation(1f, 1f);
        float[] location = locationHandler.getLocation();
        assertThat("Should be same location that was stored",
                   location, equalTo(new float[] { 1f, 1f }));
    }

    @Test public void testRecoverFromIncorrectValue() {
        setPreference("stuff");
        assertThat("Should use default value",
                   locationHandler.getLocation(),
                   equalTo(new float[] { 0f, 0f }));

        setPreference("some:stuff");
        assertThat("Should use default value",
                   locationHandler.getLocation(),
                   equalTo(new float[] { 0f, 0f }));

        setPreference("0");
        assertThat("Should use default value",
                   locationHandler.getLocation(),
                   equalTo(new float[] { 0f, 0f }));

        setPreference("1:2:3");
        assertThat("Should use default value",
                   locationHandler.getLocation(),
                   equalTo(new float[] { 0f, 0f }));
    }

    @Test public void testParseEmpty() {
        PreferenceManager
            .getDefaultSharedPreferences(context)
            .edit()
            .remove(Settings.PREF_LOCATION)
            .commit();
        assertThat("Should use default value",
                   locationHandler.getLocation(),
                   equalTo(new float[] { 0f, 0f }));
    }

    private void setPreference(String value) {
        PreferenceManager
            .getDefaultSharedPreferences(context)
            .edit()
            .putString(Settings.PREF_LOCATION, value)
            .commit();
    }
}
