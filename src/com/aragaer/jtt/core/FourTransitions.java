package com.aragaer.jtt.core;

import android.os.Parcel;
import android.os.Parcelable;

public class FourTransitions implements Parcelable {
	public final long previousStart, currentStart, currentEnd, nextEnd;
	public final boolean isDayCurrently;

	public FourTransitions(long previousStart, long currentStart,
			long currentEnd, long nextEnd, boolean isDayCurrently) {
		this.previousStart = previousStart;
		this.currentStart = currentStart;
		this.currentEnd = currentEnd;
		this.nextEnd = nextEnd;
		this.isDayCurrently = isDayCurrently;
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

	public FourTransitions shiftToPast(long transition) {
		return new FourTransitions(transition, previousStart, currentStart,
				currentEnd, !isDayCurrently);
	}

	public FourTransitions shiftToFuture(long transition) {
		return new FourTransitions(currentStart, currentEnd, nextEnd,
				transition, !isDayCurrently);
	}
}
