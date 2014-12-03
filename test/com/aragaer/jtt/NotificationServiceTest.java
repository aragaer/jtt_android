package com.aragaer.jtt;
// vim: et ts=4 sts=4 sw=4

import java.util.List;

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.*;

import android.content.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;


@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk=18)
public class NotificationServiceTest {
    @Test
    public void testBroadcastReceiverRegistered() {
        List<ShadowApplication.Wrapper> registeredReceivers = Robolectric.getShadowApplication().getRegisteredReceivers();

        assertFalse(registeredReceivers.isEmpty());

        String name = NotificationService.class.getSimpleName();

        for (ShadowApplication.Wrapper wrapper : registeredReceivers)
            if (name.equals(wrapper.broadcastReceiver.getClass().getSimpleName()))
                return;

        fail("receiver not found");
    }
}
