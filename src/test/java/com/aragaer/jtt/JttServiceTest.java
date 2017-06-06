// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

import java.util.LinkedList;

import android.app.Service;
import android.content.*;
import android.preference.PreferenceManager;
import android.os.Handler;
import android.util.Log;

import org.junit.*;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.aragaer.jtt.android.AndroidTicker;


@RunWith(PowerMockRunner.class)
@PrepareForTest({AndroidTicker.class, Log.class, Service.class,
            PreferenceManager.class})
public class JttServiceTest {

    private static Context mockContext = mock(Context.class);
    private static SharedPreferences mockPref = mock(SharedPreferences.class);

    private TestJttService service;

    @Before public void setUp() throws Exception {
        mockStatic(Log.class);
        mockStatic(PreferenceManager.class);
        when(PreferenceManager.getDefaultSharedPreferences(any(Context.class)))
            .thenReturn(mockPref);
        when(mockPref.getString(Settings.PREF_LOCATION, "0.0:0.0"))
            .thenReturn("0.0:0.0");
        suppress(method(Handler.class, "sendEmptyMessage"));
        suppress(method(Service.class, "onCreate"));

        service = spy(new TestJttService());
    }

    @Test public void testCreate() {
        service.onCreate();
        verify((Service) service).onCreate();
        assertEquals("Two receivers registered in onCreate",
                     2, service.receivers.size());
    }

    @Test public void testStart() {
        service.onCreate();
        service.resetReceivers();
        int result = service.onStartCommand(null, 0, 0);
        assertEquals("onStartCommand returns START_STICKY",
                     Service.START_STICKY, result);
        assertEquals("No receivers registered in onStartCommand",
                     0, service.receivers.size());
    }

    private class TestJttService extends JttService {
        LinkedList<BroadcastReceiver> receivers
            = new LinkedList<BroadcastReceiver>();
        LinkedList<IntentFilter> filters = new LinkedList<IntentFilter>();

        @Override public Intent registerReceiver(BroadcastReceiver receiver,
                                                 IntentFilter intentFilter) {
            receivers.add(receiver);
            filters.add(intentFilter);
            return null;
        }

        public void resetReceivers() {
            receivers.clear();
            filters.clear();
        }
    }
}
