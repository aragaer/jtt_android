package com.aragaer.jtt;

import android.content.Context;
import android.content.res.Resources;

public class JTTHour {
    public static final String Glyphs[] = { "酉", "戌", "亥", "子", "丑", "寅", "卯",
            "辰", "巳", "午", "未", "申" };

    public final Boolean isNight;
    public final int num; // 0 to 11, where 0 is hour of Cock and 11 is hour of Monkey
    public final int strikes;
    public int fraction; // 0 to 99

    private static final int num_to_strikes(int num) {
        return 9 - ((num - 3) % 6);
    }

    public JTTHour(int num) {
        this(num, 0);
    }

    public JTTHour(int n, int f) {
        num = n;
        isNight = n < 6;
        strikes = num_to_strikes(n);
        fraction = f;
    }

    public static class StringsHelper {
        private final String DayHours[], NightHours[];
        private final String Hours[], HrOf[];

        public String getHour(int num) {
            return Hours[num];
        }
        
        public String getHrOf(int num) {
            return HrOf[num];
        }

        public StringsHelper(Context ctx) {
            Resources r = ctx.getResources();
            DayHours = r.getStringArray(R.array.day_hours);
            NightHours = r.getStringArray(R.array.night_hours);
            HrOf = r.getStringArray(R.array.hour_of);
            Hours = new String[12];
            System.arraycopy(NightHours, 0, Hours, 0, 6);
            System.arraycopy(DayHours, 0, Hours, 6, 6);
        }

        public int name_to_num(String name) {
            int i;
            for (i = 0; i < 6; i++)
                if (NightHours[i] == name)
                    return i;
            for (i = 0; i < 6; i++)
                if (DayHours[i] == name)
                    return i + 6;
            return -1;
        }

        public JTTHour makeHour(String hour) {
            return makeHour(hour, 0);
        }

        public JTTHour makeHour(String hour, int fraction) {
            return new JTTHour(name_to_num(hour), fraction);
        }
    }
}
