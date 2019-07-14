// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.resources;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;

import com.aragaer.jtt.R;
import com.aragaer.jtt.Settings;

import org.junit.*;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;


@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class, Configuration.class, PreferenceManager.class,
            SharedPreferences.class, Resources.class, Context.class,
            DateFormat.class})
public class RuntimeResourcesTest {

    private static final Context mockContext = mock(Context.class);
    private final SharedPreferences mockPref = mock(SharedPreferences.class);
    private final Resources resources = mock(Resources.class);
    private final Configuration config = mock(Configuration.class);

    @Before public void setUp() {
        mockStatic(Log.class);
        mockStatic(PreferenceManager.class);
        mockStatic(Resources.class);
        mockStatic(DateFormat.class);
        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        when(mockContext.getResources()).thenReturn(resources);
        when(PreferenceManager.getDefaultSharedPreferences(mockContext)).thenReturn(mockPref);
        when(mockPref.getString(Settings.PREF_HNAME, "0")).thenReturn("0");
        when(mockPref.getString(Settings.PREF_LOCALE, "")).thenReturn("");
        when(Resources.getSystem()).thenReturn(resources);
        when(resources.getConfiguration()).thenReturn(config);
        when(resources.getStringArray(R.array.hour)).thenReturn(new String[] {""});
        when(resources.getStringArray(R.array.hour_of)).thenReturn(new String[] {""});
        when(resources.getStringArray(R.array.quarter)).thenReturn(new String[] {""});
    }

    @Test public void test_canGetForContext() {
        RuntimeResources rr = RuntimeResources.get(mockContext);
        assertNotNull(rr);
    }

    @Test public void test_isSingleton() {
        RuntimeResources rr = RuntimeResources.get(mockContext);
        RuntimeResources rr2 = RuntimeResources.get(mockContext);

        assertEquals(rr, rr2);
    }

    @Test public void test_canGetStringResources() {
        RuntimeResources rr = RuntimeResources.get(mockContext);
        StringResources sr = rr.getStringResources();

        assertNotNull(sr);
    }
}
