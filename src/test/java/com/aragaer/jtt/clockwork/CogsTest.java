package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import org.junit.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


public class CogsTest {

    private Cogs cogs;
    private TestChime chime;

    @Before public void setUp() {
        cogs = new Cogs();
        chime = new TestChime();
        cogs.attachChime(chime);
    }

    @Test public void shouldInitializeWithZero() {
        cogs.rotate(0);

        assertThat(chime.getLastTick(), equalTo(0));
    }

    @Test public void shouldPassNewValue() {
        cogs.rotate(1);

        assertThat(chime.getLastTick(), equalTo(1));
    }

    @Test public void shouldAccumulateProgress() {
        cogs.rotate(1);
        cogs.rotate(1);

        assertThat(chime.getLastTick(), equalTo(2));
    }
}
