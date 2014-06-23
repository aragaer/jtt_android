package com.aragaer.jtt.transition;

public class Day implements DayInterval {

	private Sunrise sunrise;
	private Sunset sunset;

	public Day(Sunrise start, Sunset end) {
		sunrise = start;
		sunset = end;
	}

	@Override
	public Transition getStart() {
		return sunrise;
	}

	@Override
	public Transition getEnd() {
		return sunset;
	}

	@Override
	public boolean isDay() {
		return true;
	}
}
