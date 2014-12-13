package com.aragaer.jtt;
// vim: et ts=4 sts=4 sw=4

import java.util.List;

import org.junit.*;
import org.junit.runner.RunWith;

import org.robolectric.annotation.Config;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.*;
import org.robolectric.util.ActivityController;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import android.app.Activity;
import android.content.*;

import static com.aragaer.jtt.clockwork.android.Chime.ACTION_JTT_TICK;


@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class MainActivityTest {

    Activity activity;

    @Before
    public void setup() {
    }

    @Test
    public void shouldBuild() {
        ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class);
    }

    // FIXME: Everything else fails because of shadow layer styles in theme
    public void shouldCreate() {
        ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).create();
    }

    public void shouldStart() {
        ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).create().start();
    }

    public void shouldRegisterTickListener() {
        ShadowApplication shadowApplication = Robolectric.getShadowApplication();
        Intent intent = new Intent(ACTION_JTT_TICK);
        List<BroadcastReceiver> receiversForIntent = shadowApplication.getReceiversForIntent(intent);
        assertThat(receiversForIntent.size(), equalTo(1));
    }
}
