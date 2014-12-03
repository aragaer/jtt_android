package com.aragaer.jtt.core;
// vim: et ts=4 sts=4 sw=4

public class JttTime {

    public static enum Hour {
        COCK, DOG, BOAR, RAT, OX, TIGER, HARE, DRAGON, SERPENT, HORSE, RAM, MONKEY
    };

    public static enum Quarter {
        FIRST, SECOND, THIRD, FOURTH
    };

    public static final int INTERVALS_PER_DAY = 2;
    public static final int HOURS_PER_INTERVAL = 6;
    public static final int QUARTERS_PER_HOUR = 4;
    public static final int TICKS_PER_QUARTER = 10;
    public static final int TICKS_PER_HOUR = TICKS_PER_QUARTER * QUARTERS_PER_HOUR;
    public static final int TICKS_PER_INTERVAL = TICKS_PER_HOUR * HOURS_PER_INTERVAL;
    public static final int TICKS_PER_DAY = TICKS_PER_INTERVAL * INTERVALS_PER_DAY;

    public final Hour hour;
    public final Quarter quarter;

    private JttTime(int ticks) {
        int modifiedTicks = ticks + TICKS_PER_QUARTER * 2;
        if (modifiedTicks >= TICKS_PER_DAY)
            modifiedTicks -= TICKS_PER_DAY;

        int hourNumber = modifiedTicks / TICKS_PER_HOUR;
        hour = Hour.values()[hourNumber];

        int hourTicks = modifiedTicks - hourNumber * TICKS_PER_HOUR;
        int quarterNumber = hourTicks / TICKS_PER_QUARTER;
        quarter = Quarter.values()[quarterNumber];
    }

    public static JttTime fromTicks(int ticks) {
        return new JttTime(ticks);
    }
}
