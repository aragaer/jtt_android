package com.aragaer.jtt.transition;

public class Sunset implements Transition {
	private long time;

	public Sunset(long timestamp) {
		time = timestamp;
	}

	@Override
	public boolean isSunrise() {
		return false;
	}

	@Override
	public long getTimestamp() {
		return time;
	}

}
