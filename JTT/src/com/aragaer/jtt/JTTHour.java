package com.aragaer.jtt;

import android.os.Parcel;
import android.os.Parcelable;

public class JTTHour implements Parcelable {
    public static final String DayHours[] = { "Hare", "Dragon", "Serpent",
            "Horse", "Ram", "Monkey" };
    public static final String NightHours[] = { "Cock", "Dog", "Boar", "Rat",
            "Ox", "Tiger" };
    public static final String Glyphs[] = { "酉", "戌", "亥", "子", "丑", "寅", "卯",
            "辰", "巳", "午", "未", "申" };
    public static final String Hours[] = { "Cock", "Dog", "Boar", "Rat", "Ox",
            "Tiger", "Hare", "Dragon", "Serpent", "Horse", "Ram", "Monkey" };

    public Boolean isNight = false;
    public String hour;
    public int num; // 0 to 11, where 0 is hour of Cock and 11 is hour of Monkey
    public int strikes;
    public float fraction;

    public static int name_to_num(String name) {
        int i;
        for (i = 0; i < NightHours.length; i++)
            if (NightHours[i] == name)
                return i;
        for (i = 0; i < DayHours.length; i++)
            if (DayHours[i] == name)
                return i + 6;
        return -1;
    }

    private int num_to_strikes(int num) {
        num %= 6;
        return (num < 3 ? 6 : 12) - num;
    }

    public JTTHour(String hour) {
        this(hour, 0);
    }

    public JTTHour(String h, float f) {
        hour = h;
        num = name_to_num(h);
        isNight = num < 6;
        strikes = num_to_strikes(num);
        fraction = f;
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
        hour = Hours[n];
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
