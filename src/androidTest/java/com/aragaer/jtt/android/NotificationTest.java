package com.aragaer.jtt.android;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.test.uiautomator.*;

import androidx.test.filters.LargeTest;

import com.aragaer.jtt.Settings;
import com.aragaer.jtt.mechanics.AndroidTicker;

import org.junit.*;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@LargeTest
public class NotificationTest {

    private UiDevice device;
    private Context context;

    @Before public void setUp() {
        Instrumentation instrumentation = getInstrumentation();
        device = UiDevice.getInstance(instrumentation);
        context = instrumentation.getTargetContext();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings.edit().putString(Settings.PREF_LOCALE, "1").commit();
    }

    @After public void cleanUp() {
        context.removeStickyBroadcast(new Intent(AndroidTicker.ACTION_JTT_TICK));
        device.pressHome();
    }

    private void setNotificationPreference(boolean value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings.edit().putBoolean(Settings.PREF_NOTIFY, value).commit();
    }

    @Test public void testHasNotificationIfEnabled() {
        setNotificationPreference(true);
        device.openNotification();
        device.wait(Until.hasObject(By.textContains("Hour of the")), 1000);
        UiObject2 notification = device.findObject(By.textContains("Hour of the"));
        assertNotNull("Notification can be found", notification);
    }

    @Test public void testNoNotificationIfDisabled() {
        setNotificationPreference(false);
        device.openNotification();
        device.wait(Until.hasObject(By.textContains("Hour of the")), 1000);
        UiObject2 notification = device.findObject(By.textContains("Hour of the"));
        assertNull("Notification can not be found", notification);
    }
}
