package com.aragaer.jtt.clockwork.android;
// vim: et ts=4 sts=4 sw=4

import java.lang.Thread;

import org.junit.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.aragaer.jtt.clockwork.TickClient;
import com.aragaer.jtt.clockwork.TickCounter;


public class ClockTickCallbackTest {

    private TickCounter counter;
    private TestTickClient client;

    @Before public void setUp() {
        client = new TestTickClient();
        counter = new TickCounter();
        counter.addClient(client);
    }

    @Test public void shouldTrigger0TicksIfLessThanLength() {
        long now = System.currentTimeMillis();
        ClockTickCallback callback = new ClockTickCallback(counter, now, 10000);
        callback.onTick();
        assertThat(client.ticks, equalTo(0));
    }

    @Test public void shouldTriggerRequiredNumberOfTicks() {
        long offset = 1000 * 42 + 250;
        long now = System.currentTimeMillis();
        ClockTickCallback callback = new ClockTickCallback(counter, now - offset, 1000);
        callback.onTick();
        assertThat(client.ticks, equalTo(42));
    }

    @Test public void shouldTickOnceAgain() throws InterruptedException {
        long offset = 1000 * 42 + 997;
        long now = System.currentTimeMillis();
        ClockTickCallback callback = new ClockTickCallback(counter, now - offset, 1000);
        callback.onTick();
        assertThat(client.ticks, equalTo(42));
        Thread.sleep(5);
        callback.onTick();
        assertThat(client.ticks, equalTo(43));
    }

    private static class TestTickClient implements TickClient {
        public int ticks;
        public void tickChanged(int ticks) {
            this.ticks = ticks;
        }
    }
}
