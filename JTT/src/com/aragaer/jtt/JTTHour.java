package com.aragaer.jtt;

import android.os.Parcel;
import android.os.Parcelable;

public class JTTHour implements Parcelable {
    public static final String Glyphs[] = { "酉", "戌", "亥", "子", "丑", "寅", "卯",
            "辰", "巳", "午", "未", "申" };

    public Boolean isNight = false;
    public int num; // 0 to 11, where 0 is hour of Cock and 11 is hour of Monkey
    public int strikes;
    public float fraction;

    private int num_to_strikes(int num) {
        return 9 - ((num - 3) % 6);
    }

    public JTTHour(float num) {
        this((int) num, num - (int) num);
    }

    public JTTHour(int num) {
        this(num, 0);
    }

    public JTTHour(int n, float f) {
        num = n;
        isNight = n < 6;
        strikes = num_to_strikes(n);
        fraction = f;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeFloat(num + fraction);
    }

    public static final Parcelable.Creator<JTTHour> CREATOR = new Parcelable.Creator<JTTHour>() {
        public JTTHour createFromParcel(Parcel in) {
            return new JTTHour(in.readFloat());
        }

        public JTTHour[] newArray(int size) {
            return new JTTHour[size];
        }
    };

}
