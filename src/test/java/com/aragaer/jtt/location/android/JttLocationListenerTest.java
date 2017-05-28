// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.location.android;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

import android.location.Location;
import android.location.LocationManager;
import android.content.Context;
import android.view.View;

import org.junit.*;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import com.aragaer.jtt.LocationPreference;


@RunWith(PowerMockRunner.class)
public class JttLocationListenerTest {
    private JttLocationListener listener;
    private TestLocationPreference pref;

    private static Context mockContext = mock(Context.class);

    private LocationManager mockLM;

    @Before public void setUp() {
        pref = spy(new TestLocationPreference());
        mockLM = mock(LocationManager.class);
        when(mockContext.getSystemService(Context.LOCATION_SERVICE)).thenReturn(mockLM);

        listener = new JttLocationListener(mockContext, pref);
    }

    @Test public void testAcquireLocation() {
        when(mockLM.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(true);

        listener.acquireLocation();

        verify(mockLM).requestLocationUpdates(eq(LocationManager.NETWORK_PROVIDER), eq(0L), eq(0f),
                                              eq(listener));
    }

    @Test public void testUseLastKnownLocation() {
        Location mockLocation = mock(Location.class);
        when(mockLM.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(true);
        when(mockLM.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)).thenReturn(mockLocation);

        listener.acquireLocation();

        verify(mockLM).getLastKnownLocation(eq(LocationManager.NETWORK_PROVIDER));
        verify(mockLM).requestLocationUpdates(eq(LocationManager.NETWORK_PROVIDER), eq(0L), eq(0f),
                                              eq(listener));
        assertEquals(pref.location, mockLocation);
        assertFalse(pref.stop);
    }

    private static class TestLocationPreference extends LocationPreference {
        public Location location;
        public boolean stop;

        public String getPersistedString(String value) {
            return value;
        }

        public View createDialogView() {
            return super.onCreateDialogView();
        }

        public TestLocationPreference() {
            super(null, null);
        }

        @Override public void makeUseOfNewLocation(Location l, boolean stopLocating) {
            this.location = l;
            this.stop = stopLocating;
        }
    }
}
