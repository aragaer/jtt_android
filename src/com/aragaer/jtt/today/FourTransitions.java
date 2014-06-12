package com.aragaer.jtt.today;

public class FourTransitions {
	private long previousStart, currentStart, currentEnd, nextEnd;

	public long getPreviousStart() {
		return previousStart;
	}

	public long getCurrentStart() {
		return currentStart;
	}

	public long getCurrentEnd() {
		return currentEnd;
	}

	public long getNextEnd() {
		return nextEnd;
	}

	public FourTransitions(long[] transitions) {
		previousStart = transitions[0];
		currentStart = transitions[1];
		currentEnd = transitions[2];
		nextEnd = transitions[3];
	}

	public boolean isInCurrentInterval(long timestamp) {
		return timestamp >= currentStart && timestamp < currentEnd;
	}

	public boolean notInCurrentInterval(long timestamp) {
		return !isInCurrentInterval(timestamp);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof FourTransitions))
			return false;
		FourTransitions otherTransitions = (FourTransitions) other;
		return previousStart == otherTransitions.previousStart
				&& currentStart == otherTransitions.currentStart
				&& currentEnd == otherTransitions.currentEnd
				&& nextEnd == otherTransitions.nextEnd;
	}
}
