package com.aragaer.jtt.core;

public class Hour {
	public static final int HOURS = 6,
			QUARTERS = 4,
			QUARTER_PARTS = 10,
			HOUR_PARTS = QUARTERS * QUARTER_PARTS,
			TOTAL_PARTS = HOURS * HOUR_PARTS * 2;
	public static final String Glyphs[] = "酉戌亥子丑寅卯辰巳午未申".split("(?!^)");

	public int num, // 0 to 11, where 0 is hour of Cock and 11 is hour of Monkey
		quarter, // 0 to 3
		quarter_parts, // 0 to PARTS
		wrapped; // wrapped into single integer

	private Hour() { } // just allocate

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
		wrapped = (num * QUARTERS + quarter - 2) * QUARTER_PARTS + quarter_parts;
		wrapped = (wrapped + TOTAL_PARTS) % TOTAL_PARTS;
	}

	public static Hour fromTimestamps(final long tr[], final boolean is_day, final long now, Hour reuse) {
		final double passed = (1. * now - tr[1]) / (tr[2] - tr[1]) + (is_day ? 1 : 0);
		return fromWrapped((int) (HOURS * HOUR_PARTS * passed), reuse);
	}

	public static Hour fromWrapped(final int f, Hour reuse) {
		final int q = f / QUARTER_PARTS + 2;
		final int n = q / QUARTERS;
		if (reuse == null)
			reuse = new Hour();
		reuse.setTo(n % Glyphs.length, q % QUARTERS, f % QUARTER_PARTS);
		return reuse;
	}

	/* truncate new value according to granularity
	 * if new value is different from current, update and return true
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
		return (hour + HOURS - 1) % HOURS;
	}

	/* 0-11 -> 0-5 */
	public static final int upperBoundary(final int hour) {
		return hour % HOURS;
	}


	/* given start and end of time interval return hour boundary for given position */
	public static long getHourBoundary(final long start, final long end, final int pos) {
		final long half_hlen = (end - start) / HOURS / 2;
		return start + half_hlen * pos * 2 + half_hlen;
	}
}
