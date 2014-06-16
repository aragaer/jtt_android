package com.aragaer.jtt.core;

public class DayInterval {
	private final long start, end;
	private final boolean isDay;

	public DayInterval(long start, long end, boolean isDay) {
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
