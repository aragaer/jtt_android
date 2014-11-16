package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import android.content.*;


@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class BroadcastClockEventTest {

    private BroadcastClockEvent event;
    private TestReceiver receiver;

    @Before
    public void setup() {
        event = new BroadcastClockEvent(Robolectric.application, "Test event", 1);
        receiver = new TestReceiver();
        Robolectric.application.registerReceiver(receiver, new IntentFilter("Test event"));
    }

    static class TestReceiver extends BroadcastReceiver {
        int wrapped = -1;
        public void onReceive(Context context, Intent intent) {
            if (!intent.getAction().equals("Test event"))
                return;
            wrapped = intent.getIntExtra("jtt", 0);
        }
    }

    @Test
    public void shouldSendBroadcast() {
        assertThat(receiver.wrapped, equalTo(-1));
        event.trigger(0);
        assertThat(receiver.wrapped, equalTo(0));
    }

    @Test
    public void shouldSendBroadcastWithCorrectTime() {
        event.trigger(42);
        assertThat(receiver.wrapped, equalTo(42));
    }
}
