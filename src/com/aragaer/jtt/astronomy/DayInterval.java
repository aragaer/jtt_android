package com.aragaer.jtt.astronomy;
// vim: et ts=4 sts=4 sw=4


public class DayInterval {
    private final long start;
    private final long end;
    private final boolean isDay;

    private DayInterval(long start, long end, boolean isDay) {
        this.start = start;
        this.end = end;
        this.isDay = isDay;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public boolean isDay() {
        return isDay;
    }

    public static DayInterval Day(long sunrise, long sunset) {
        return new DayInterval(sunrise, sunset, true);
    }

    public static DayInterval Night(long sunset, long sunrise) {
        return new DayInterval(sunset, sunrise, false);
    }

	public long getLength() {
		return end - start;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof DayInterval) {
			DayInterval otherInterval = (DayInterval) other;
			return this.end == otherInterval.end
					&& this.start == otherInterval.start
					&& this.isDay == otherInterval.isDay;
		}
		return false;
	}

	public boolean contains(long timestamp) {
		return start <= timestamp && timestamp < end;
	}

}
