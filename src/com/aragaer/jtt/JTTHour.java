package com.aragaer.jtt;

public class JTTHour {
	public static final String Glyphs[] = { "酉", "戌", "亥", "子", "丑", "寅", "卯",
		"辰", "巳", "午", "未", "申" };

	public boolean isNight;
	public int num; // 0 to 11, where 0 is hour of Cock and 11 is hour of Monkey
	public int strikes;
	public int fraction; // 0 to 99

	private static final int num_to_strikes(int num) {
		return 9 - ((num - 3) % 6);
	}

	public JTTHour(int num) {
		this(num, 0);
	}

	public JTTHour(int n, int f) {
		this.setTo(n, f);
	}

	// Instead of reallocation, reuse existing object
	public void setTo(int n, int f) {
		num = n;
		isNight = n < 6;
		strikes = num_to_strikes(n);
		fraction = f;
	}
}
