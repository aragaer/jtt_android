package com.aragaer.jtt;

import android.content.Context;
import android.content.res.Resources;

public class JTTHourStringsHelper {
    private String DayHours[], NightHours[];
    private String Hours[], HrOf[];

    public String getHour(int num) {
        return Hours[num];
    }
    
    public String getHrOf(int num) {
        return HrOf[num];
    }

    public JTTHourStringsHelper(Context ctx) {
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

    public JTTHour makeHour(String hour, float fraction) {
        return new JTTHour(name_to_num(hour), fraction);
    }
}
