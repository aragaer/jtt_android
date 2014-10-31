package com.aragaer.jtt.core;

public class Hour {
	public static final int TOTAL_PARTS = JttTime.HOURS_PER_INTERVAL * JttTime.TICKS_PER_HOUR * 2;
	public static final String Glyphs[] = "酉戌亥子丑寅卯辰巳午未申".split("(?!^)");

	public int num, // 0 to 11, where 0 is hour of Cock and 11 is hour of Monkey
			quarter, // 0 to 3
			quarter_parts, // 0 to PARTS
			wrapped; // wrapped into single integer

	private Hour() {
	} // just allocate

	public Hour(int num) {
		this(num, 0, 0);
	}

	public Hour(int num, int q, int f) {
		setTo(num, q, f);
	}

	// Instead of reallocation, reuse existing object
	public void setTo(int n, int q, int f) {
		num = n;
		quarter = q;
		quarter_parts = f;
		wrapped = (num * JttTime.QUARTERS_PER_HOUR + quarter - 2) * JttTime.TICKS_PER_QUARTER
				+ quarter_parts;
		wrapped = (wrapped + TOTAL_PARTS) % TOTAL_PARTS;
	}

	public static Hour fromTransitions(ThreeIntervals transitions,
			final long now, Hour reuse) {
		long timeSinceStart = now - transitions.getCurrentStart();
		long intervalLength = transitions.getCurrentEnd()
				- transitions.getCurrentStart();
		double fractionPassed = (1. * timeSinceStart) / intervalLength;
		int ticksPassed = (int) (JttTime.TICKS_PER_INTERVAL * fractionPassed);
		int wrappedValue = transitions.isDayCurrently() ? ticksPassed + JttTime.TICKS_PER_INTERVAL
				: ticksPassed;
		return fromWrapped(wrappedValue, reuse);
	}

	public static Hour fromWrapped(final int f, Hour reuse) {
		final int q = f / JttTime.TICKS_PER_QUARTER + 2;
		final int n = q / JttTime.QUARTERS_PER_HOUR;
		if (reuse == null)
			reuse = new Hour();
		reuse.setTo(n % Glyphs.length, q % JttTime.QUARTERS_PER_HOUR, f % JttTime.TICKS_PER_QUARTER);
		return reuse;
	}

	/*
	 * truncate new value according to granularity if new value is different
	 * from current, update and return true
	 */
	public boolean compareAndUpdate(int new_wrapped, final int granularity) {
		new_wrapped -= new_wrapped % granularity;
		if (wrapped == new_wrapped)
			return false;
		fromWrapped(new_wrapped, this);
		return true;
	}

	/* 0, 6 -> 5; 1-5, 7-11 -> 0-4 */
	public static final int lowerBoundary(final int hour) {
		return (hour + JttTime.HOURS_PER_INTERVAL - 1) % JttTime.HOURS_PER_INTERVAL;
	}

	/* 0-11 -> 0-5 */
	public static final int upperBoundary(final int hour) {
		return hour % JttTime.HOURS_PER_INTERVAL;
	}

	/*
	 * given start and end of time interval return hour boundary for given
	 * position
	 */
	public static long getHourBoundary(final long start, final long end,
			final int pos) {
		final long half_hlen = (end - start) / JttTime.HOURS_PER_INTERVAL / 2;
		return start + half_hlen * pos * 2 + half_hlen;
	}
}
