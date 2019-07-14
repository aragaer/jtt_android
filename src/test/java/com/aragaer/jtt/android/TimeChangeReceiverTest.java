// -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; -*-
// vim: et ts=4 sts=4 sw=4 syntax=java
package com.aragaer.jtt.android;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

import android.content.*;
import android.os.Handler;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.aragaer.jtt.mechanics.AndroidTicker;


@RunWith(PowerMockRunner.class)
@PrepareForTest(AndroidTicker.class)
public class TimeChangeReceiverTest {

    private Context mockContext;
    private Intent mockIntent;
    private TimeChangeReceiver receiver;

    @Before public void setUp() {
        receiver = new TimeChangeReceiver();
        mockIntent = mock(Intent.class);
        mockContext = mock(Context.class);
    }

    @Test public void testReceiveNoAction() {
        receiver.onReceive(mockContext, mockIntent);
        verify(mockContext, never()).startService(any(Intent.class));
    }

    @Test public void testReceiveTimeChanged() {
        ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);
        suppress(method(Handler.class, "sendEmptyMessage"));
        when(mockIntent.getAction()).thenReturn(Intent.ACTION_TIME_CHANGED);
        receiver.onReceive(mockContext, mockIntent);
        verify(mockContext).startService(captor.capture());
        // Instrumentation test checks that this actually triggers Jtt Tick event
    }

    @Test public void testReceiveDateChanged() {
        ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);
        suppress(method(Handler.class, "sendEmptyMessage"));
        when(mockIntent.getAction()).thenReturn(Intent.ACTION_TIME_CHANGED);
        receiver.onReceive(mockContext, mockIntent);
        verify(mockContext).startService(captor.capture());
        // Instrumentation test checks that this actually triggers Jtt Tick event
    }

    @Test public void testReceiveSomethingElse() {
        when(mockIntent.getAction()).thenReturn(Intent.ACTION_BOOT_COMPLETED);
        receiver.onReceive(mockContext, mockIntent);
        verify(mockContext, never()).startService(any(Intent.class));
    }
}
