package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4


public class TestClock extends Clock {

    public int ticks;
    private final TestChime chime;

    public TestClock() {
        this(new TestChime());
    }

    private TestClock(TestChime chime) {
        super(null, chime, new TestMetronome());
        this.chime = chime;
    }

    @Override
    public void tick(int ticks) {
        super.tick(ticks);
        this.ticks = chime.getLastTick();
    }
}
