package com.aragaer.jtt.transition;

public class Sunrise implements Transition {
	private long time;

	public Sunrise(long timestamp) {
		time = timestamp;
	}

	@Override
	public boolean isSunrise() {
		return true;
	}

	@Override
	public long getTimestamp() {
		return time;
	}

}
