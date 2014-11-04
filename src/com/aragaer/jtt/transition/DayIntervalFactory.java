package com.aragaer.jtt.transition;

import com.aragaer.jtt.astronomy.DayInterval;


public interface DayIntervalFactory {
	public DayInterval getIntervalForTimestamp(long timestamp);
}
