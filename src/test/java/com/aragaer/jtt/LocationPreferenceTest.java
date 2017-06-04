// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

import android.app.AlertDialog;
import android.app.Dialog;
import android.location.Location;
import android.location.LocationManager;
import android.content.Context;
import android.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.junit.*;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.aragaer.jtt.location.android.JttLocationListener;


@RunWith(PowerMockRunner.class)
public class LocationPreferenceTest {

    private static Context mockContext = mock(Context.class);
    private static AlertDialog mockDialog = mock(AlertDialog.class);
    private static LayoutInflater layoutInflater = mock(LayoutInflater.class);
    private static View view = mock(View.class);

    private TestLocationPreference pref;
    private LocationManager mockLM;
    private TextView lat, lon;

    @Before public void setUp() {
        pref = new TestLocationPreference();
        mockLM = mock(LocationManager.class);
        lat = spy(mock(TextView.class));
        lon = spy(mock(TextView.class));

        when(mockContext.getSystemService(Context.LOCATION_SERVICE)).thenReturn(mockLM);
        when(mockContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).thenReturn(layoutInflater);
        when(layoutInflater.inflate(R.layout.location_picker, null)).thenReturn(view);
        when(view.findViewById(R.id.lat)).thenReturn(lat);
        when(view.findViewById(R.id.lon)).thenReturn(lon);

        pref.createDialogView();
        reset(lat);
        reset(lon);
    }

    @Test public void testAcquireLocation() {
        when(mockLM.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(true);

        pref.onClick(null, Dialog.BUTTON_NEUTRAL);

        verify(mockLM).requestLocationUpdates(eq(LocationManager.NETWORK_PROVIDER), eq(0L), eq(0f),
                                              any(JttLocationListener.class));
    }

    @Test public void testUseLastKnownLocation() {
        Location mockLocation = mock(Location.class);
        when(mockLocation.getLatitude()).thenReturn(1.2);
        when(mockLocation.getLongitude()).thenReturn(3.4);
        when(mockLM.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(true);
        when(mockLM.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)).thenReturn(mockLocation);

        pref.onClick(null, Dialog.BUTTON_NEUTRAL);

        verify(mockLM).getLastKnownLocation(eq(LocationManager.NETWORK_PROVIDER));
        verify(mockLM).requestLocationUpdates(eq(LocationManager.NETWORK_PROVIDER), eq(0L), eq(0f),
                                              any(JttLocationListener.class));
        verify(lat).setText(eq("1.20"));
        verify(lon).setText(eq("3.40"));
    }

    private static class TestLocationPreference extends LocationPreference {
        public String getPersistedString(String value) {
            return value;
        }

        public View createDialogView() {
            return super.onCreateDialogView();
        }

        public TestLocationPreference() {
            super(mockContext, null);
        }
    }
}
