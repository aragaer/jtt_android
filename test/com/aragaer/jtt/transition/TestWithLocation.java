package com.aragaer.jtt.transition;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class TestWithLocation extends TestWatcher {
	private Location location;

	@Override
	protected void starting(Description description) {
		location = description.getAnnotation(Location.class);
	}

	public double getLatitude() {
		return location.latitude();
	}

	public double getLongitude() {
		return location.longitude();
	}
}