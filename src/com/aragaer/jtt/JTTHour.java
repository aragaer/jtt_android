package com.aragaer.jtt;

public class JTTHour {
	public static final int QUARTERS = 4;
	public static final int PARTS = 100; // split quarter to that much parts

	/* compatibility calculations */
	private static final int COMPAT_COEFF = QUARTERS * PARTS / 100;

	public static final String Glyphs[] = { "酉", "戌", "亥", "子", "丑", "寅", "卯",
		"辰", "巳", "午", "未", "申" };

	public boolean isNight;
	public int num; // 0 to 11, where 0 is hour of Cock and 11 is hour of Monkey
	public int strikes;
	public int percent; // 0 to 99 - compatibility
	public int quarter; // 0 to 3
	public int quarter_parts; // 0 to PARTS

	private static final int num_to_strikes(int num) {
		return 9 - ((num - 3) % 6);
	}

	public JTTHour(int num) {
		this(num, QUARTERS / 2, 0);
	}

	public JTTHour(int n, int f) {
		this.setTo(n, f);
	}

	public JTTHour(int n, int q, int f) {
		this.setTo(n, q, f);
	}

	// compatibility method
	public void setTo(int n, int f) {
		f *= COMPAT_COEFF;
		int q = f / PARTS;
		f -= q * PARTS;
		setTo(n, q, f);
	}

	// Instead of reallocation, reuse existing object
	public void setTo(int n, int q, int f) {
		num = n;
		isNight = n < 6;
		strikes = num_to_strikes(n);
		quarter = q;
		quarter_parts = f;
		percent = (q * PARTS + quarter_parts) / COMPAT_COEFF;
	}
}
