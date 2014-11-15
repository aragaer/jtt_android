package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import android.app.AlarmManager;
import android.content.*;


@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class AndroidBellTest {

    private AndroidBell clockwork;
    private TestReceiver receiver;

    @Before
    public void setup() {
        clockwork = new AndroidBell(Robolectric.application);
        receiver = new TestReceiver();
        Robolectric.application.registerReceiver(receiver, new IntentFilter(AndroidBell.ACTION_JTT_TICK));
    }

    static class TestReceiver extends BroadcastReceiver {
        int wrapped = -1;
        public void onReceive(Context context, Intent intent) {
            if (!intent.getAction().equals(AndroidBell.ACTION_JTT_TICK))
                return;
            wrapped = intent.getIntExtra("jtt", 0);
        }
    }

    @Test
    public void shouldSendBroadcast() {
        assertThat(receiver.wrapped, equalTo(-1));
        clockwork.ring(0);
        assertThat(receiver.wrapped, equalTo(0));
    }

    @Test
    public void shouldSendBroadcastWithCorrectTime() {
        clockwork.ring(42);
        assertThat(receiver.wrapped, equalTo(42));
    }
}
