package com.aragaer.jtt;

public class JTTHour {
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
        return (num < 3 ? 6 : 12) - num; 
    }

    public JTTHour(String hour) {
        this(hour, 0);
    }
    
    public JTTHour(String hour, float fraction) {
        this.hour = hour;
        this.num = name_to_num(hour);
        this.isNight = this.num < 6;
        this.strikes = num_to_strikes(this.num);
        this.fraction = fraction;
    }
    
    public JTTHour(float num) {
        this((int)num, num - (int) num);
    }

    public JTTHour(int num) {
        this(num, 0);
    }
    
    public JTTHour(int num, float fraction) {
        this.num = num;
        this.isNight = this.num < 6;
        this.hour = Hours[num];
        this.strikes = num_to_strikes(num);
        this.fraction = fraction;
    }
}
