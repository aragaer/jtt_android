package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import android.content.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk=18)
public class ChimeTest {

    @Test
    public void shouldBroadcastNewInformation() {
        TestListener listener = new TestListener();
        listener.register(Robolectric.application);

        Chime chime = new Chime(Robolectric.application);
        chime.ding(123);

        assertThat("listener calls", listener.calls, equalTo(1));
        assertThat("broadcasted tick number", listener.tick, equalTo(123));
    }

    @Test
    public void shouldSendStickyBroadcast() {
        Chime chime = new Chime(Robolectric.application);
        chime.ding(123);

        TestListener listener = new TestListener();
        listener.register(Robolectric.application);

        assertThat("listener calls", listener.calls, equalTo(1));
        assertThat("broadcasted tick number", listener.tick, equalTo(123));
    }

    static class TestListener extends ChimeListener {
        int tick;
        int calls;
        public void onChime(Context context, int tick) {
            calls++;
            this.tick = tick;
        }
    }
}
