package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import java.util.ArrayList;
import java.util.List;


public class Clockwork {

    private static class AttachedBell {

        private final Bell bell;
        private final int interval;

        public AttachedBell(Bell bell, int interval) {
            this.bell = bell;
            this.interval = interval;
        }

        private int lastTriggerAt(int time) {
            return time - time % interval;
        }

        public boolean wasTriggered(int from, int to) {
            return lastTriggerAt(to) > lastTriggerAt(from);
        }

        public void ring(int ticks) {
            bell.ring(lastTriggerAt(ticks));
        }
    }

    private List<AttachedBell> bells = new ArrayList<AttachedBell>();
    private int lastTick;
    private boolean rewound;

    public void rewind() {
        rewound = true;
        lastTick = 0;
    }

    private void ringAll(int time) {
        for (AttachedBell attached : bells)
            attached.ring(time);
    }

    private void ringTriggered(int lastTime, int newTime) {
        for (AttachedBell attached : bells)
            if (attached.wasTriggered(lastTime, newTime))
                attached.ring(newTime);
    }

    public void tick(int ticks) {
        int newTick = lastTick + ticks;
        if (rewound) {
            ringAll(newTick);
            rewound = false;
        } else
            ringTriggered(lastTick, newTick);
        lastTick = newTick;
    }

    public void attachBell(Bell bell, int interval) {
        bells.add(new AttachedBell(bell, interval));
    }
}
