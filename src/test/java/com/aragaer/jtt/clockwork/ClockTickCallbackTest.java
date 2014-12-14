package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import java.lang.Thread;
import dagger.ObjectGraph;

import org.junit.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


public class ClockTickCallbackTest {

    private TestClock clock;
    private TestChime chime;

    @Before
    public void setUp() {
        ObjectGraph graph = ObjectGraph.create(new TestModule());
        clock = graph.get(TestClock.class);
        chime = (TestChime) graph.get(Chime.class);
    }

    @Test
    public void shouldTrigger0TicksIfLessThanLength() {
        long now = System.currentTimeMillis();
        ClockTickCallback callback = new ClockTickCallback(clock.getCogs(), now, 10000);
        callback.onTick();
        assertThat(chime.getLastTick(), equalTo(0));
    }

    @Test
    public void shouldTriggerRequiredNumberOfTicks() {
        long offset = 1000 * 42 + 250;
        long now = System.currentTimeMillis();
        ClockTickCallback callback = new ClockTickCallback(clock.getCogs(), now - offset, 1000);
        callback.onTick();
        assertThat(chime.getLastTick(), equalTo(42));
    }

    @Test
    public void shouldTickOnceAgain() throws InterruptedException {
        long offset = 1000 * 42 + 997;
        long now = System.currentTimeMillis();
        ClockTickCallback callback = new ClockTickCallback(clock.getCogs(), now - offset, 1000);
        callback.onTick();
        assertThat(chime.getLastTick(), equalTo(42));
        Thread.sleep(5);
        callback.onTick();
        assertThat(chime.getLastTick(), equalTo(43));
    }
}
