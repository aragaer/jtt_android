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
    public void shouldTickWithoutBells() {
        clockwork.tick(0);
    }

    @Test
    public void shouldAttachMultipleBells() {
        Bell bell1 = new TestBell();
        Bell bell2 = new TestBell();

        clockwork.attachBell(bell1, 20);
        clockwork.attachBell(bell2, 40);
    }

    @Test
    public void shouldTriggerBell() {
        TestBell bell = new TestBell();
        clockwork.attachBell(bell, 20);

        clockwork.tick(100);

        assertThat(bell.calls, equalTo(1));
    }

    @Test
    public void shouldTriggerMultipleBells() {
        TestBell bell1 = new TestBell();
        TestBell bell2 = new TestBell();
        clockwork.attachBell(bell1, 20);
        clockwork.attachBell(bell2, 40);

        clockwork.tick(100);

        assertThat(bell1.calls, equalTo(1));
        assertThat(bell2.calls, equalTo(1));
    }

    @Test
    public void shouldTriggerBellWithCorrectTime() {
        TestBell bell1 = new TestBell();
        TestBell bell2 = new TestBell();
        clockwork.attachBell(bell1, 20);
        clockwork.attachBell(bell2, 40);

        clockwork.tick(23);

        assertThat(bell1.calls, equalTo(1));
        assertThat(bell2.calls, equalTo(0));
        assertThat(bell1.lastTickValue, equalTo(20));
        assertThat(bell2.lastTickValue, equalTo(0));
    }

    @Test
    public void shouldTriggerAllAfterRewind() {
        TestBell bell1 = new TestBell();
        TestBell bell2 = new TestBell();
        clockwork.attachBell(bell1, 20);
        clockwork.attachBell(bell2, 40);

        clockwork.rewind();
        clockwork.tick(23);

        assertThat(bell1.calls, equalTo(1));
        assertThat(bell2.calls, equalTo(1));
        assertThat(bell1.lastTickValue, equalTo(20));
        assertThat(bell2.lastTickValue, equalTo(0));
    }


    @Test
    public void shouldTriggerAfterRewind() {
        TestBell bell = new TestBell();
        clockwork.attachBell(bell, 20);

        clockwork.tick(107);
        clockwork.rewind();
        clockwork.tick(83);

        assertThat(bell.calls, equalTo(2));
        assertThat(bell.lastTickValue, equalTo(80));
    }

    @Test
    public void shouldTriggerAllOnlyOncePerRewind() {
        TestBell bell = new TestBell();
        clockwork.attachBell(bell, 20);

        clockwork.rewind();
        clockwork.tick(107);
        clockwork.tick(1);

        assertThat(bell.calls, equalTo(1));
        assertThat(bell.lastTickValue, equalTo(100));
    }

    private static class TestBell implements Bell {
        int calls;
        int lastTickValue;

        public void ring(int ticks) {
            calls++;
            lastTickValue = ticks;
        }
    }
}
