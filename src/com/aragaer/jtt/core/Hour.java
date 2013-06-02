package com.aragaer.jtt.core;

public class Hour {
	public static final int HOURS = 6,
			QUARTERS = 4,
			QUARTER_PARTS = 10,
			HOUR_PARTS = QUARTERS * QUARTER_PARTS;
	public static final String Glyphs[] = { "酉", "戌", "亥", "子", "丑", "寅", "卯",
			"辰", "巳", "午", "未", "申" };

	public boolean isNight;
	public int num, // 0 to 11, where 0 is hour of Cock and 11 is hour of Monkey
		quarter, // 0 to 3
		quarter_parts, // 0 to PARTS
		wrapped; // wrapped into single integer

	/* compatibility */
	public int fraction; // 0 to 99

	public Hour(int num) {
		this(num, 0, 0);
	}

	public Hour(int num, int q, int f) {
		setTo(num, q, f);
	}

	/* compatibility */
	public Hour(int n, int f) {
		this.setTo(n, f);
	}

	/* compatibility */
	private static final int fractions_per_quarter = 100 / QUARTERS;

	// Instead of reallocation, reuse existing object
	public void setTo(int n, int f) {
		num = n;
		isNight = n < HOURS;
		quarter = f / fractions_per_quarter;
		quarter_parts = (f % fractions_per_quarter) * HOUR_PARTS / 100;
		fraction = f;

		wrapped = ((num * QUARTERS) + quarter) * QUARTER_PARTS + quarter_parts;
	}

	// Instead of reallocation, reuse existing object
	public void setTo(int n, int q, int f) {
		num = n;
		isNight = n < HOURS;
		quarter = q;
		quarter_parts = f;
		wrapped = ((num * QUARTERS) + quarter) * QUARTER_PARTS + quarter_parts;

		fraction = (q * QUARTER_PARTS + f) * 100 / HOUR_PARTS;
	}

	public static Hour fromTimestamps(final long tr[], final boolean is_day, final long now) {
		final double passed = (1. * now - tr[1]) / (tr[2] - tr[1]) + (is_day ? 1 : 0);
		return fromWrapped((int) (HOURS * HOUR_PARTS * passed));
	}

	public static Hour fromWrapped(final int f) {
		int q = f / QUARTER_PARTS;
		int n = q / QUARTERS;
		return new Hour(n, q % QUARTERS, f % QUARTER_PARTS);
	}
}
