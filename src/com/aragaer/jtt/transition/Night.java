package com.aragaer.jtt.transition;

public class Night implements DayInterval {

	private Sunrise sunrise;
	private Sunset sunset;

	public Night(Sunset start, Sunrise end) {
		sunset = start;
		sunrise = end;
	}

	@Override
	public Transition getStart() {
		return sunset;
	}

	@Override
	public Transition getEnd() {
		return sunrise;
	}

	@Override
	public boolean isDay() {
		return false;
	}
}
