package com.aragaer.jtt.core;

import android.os.Parcel;
import android.os.Parcelable;

public class FourTransitions implements Parcelable {
	private long previousStart, currentStart, currentEnd, nextEnd;
	private boolean isDayCurrently;

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

	public boolean isDay() {
		return isDayCurrently;
	}

	public FourTransitions(long[] transitions, boolean isDayCurrently) {
		previousStart = transitions[0];
		currentStart = transitions[1];
		currentEnd = transitions[2];
		nextEnd = transitions[3];
		this.isDayCurrently = isDayCurrently;
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

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(previousStart);
		dest.writeLong(currentStart);
		dest.writeLong(currentEnd);
		dest.writeLong(nextEnd);
		dest.writeByte((byte) (isDayCurrently ? 1 : 0));
	}

	public static final Parcelable.Creator<FourTransitions> CREATOR = new Parcelable.Creator<FourTransitions>() {
		public FourTransitions createFromParcel(Parcel in) {
			return new FourTransitions(in);
		}

		public FourTransitions[] newArray(int size) {
			return new FourTransitions[size];
		}
	};

	private FourTransitions(Parcel in) {
		previousStart = in.readLong();
		currentStart = in.readLong();
		currentEnd = in.readLong();
		nextEnd = in.readLong();
		isDayCurrently = in.readByte() != 0;
	}
}
