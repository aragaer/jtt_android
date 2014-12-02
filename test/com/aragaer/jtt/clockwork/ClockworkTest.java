package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import org.junit.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


public class ClockworkTest {

    private Clockwork clockwork;

    @Before
    public void setUp() {
        clockwork = new Clockwork();
    }

    @Test
    public void shouldAttachTrigger() {
        TestTrigger trigger = new TestTrigger();
        clockwork.attachTrigger(trigger, 10);

        assertThat("initial state", trigger.triggerCount, equalTo(0));

        clockwork.tick(5);
        assertThat("too early for a trigger", trigger.triggerCount, equalTo(0));

        clockwork.tick(5);
        assertThat("first trigger", trigger.triggerCount, equalTo(1));

        clockwork.tick(3);
        assertThat("a bit more time passes", trigger.triggerCount, equalTo(1));

        clockwork.tick(8);
        assertThat("got past the end of another interval", trigger.triggerCount, equalTo(2));
    }

    private static class TestTrigger implements Clockwork.Trigger {
        public int triggerCount;

        public void trigger() {
            triggerCount++;
        }
    }
}
