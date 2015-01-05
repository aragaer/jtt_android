package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import org.junit.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import static com.aragaer.jtt.core.JttTime.TICKS_PER_INTERVAL;


public class TickCounterTest {

    private TickCounter cogs;
    private TestTickClient client;

    @Before public void setUp() {
        cogs = new TickCounter();
        client = new TestTickClient();
        cogs.addClient(client);
    }

    @Test public void shouldInitializeWithZero() {
        cogs.rotate(0);

        assertThat(client.ticks, equalTo(0));
    }

    @Test public void shouldNotifyClient() {
        cogs.rotate(1);

        assertThat(client.ticks, equalTo(1));
    }

    @Test public void shouldAccumulateProgress() {
        cogs.rotate(1);
        cogs.rotate(1);

        assertThat(client.ticks, equalTo(2));
    }

    private static class TestTickClient implements TickClient {
        public int ticks;
        public void tickChanged(int ticks) {
            this.ticks = ticks;
        }
    }
}
