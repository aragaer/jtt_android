package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import com.aragaer.jtt.clockwork.android.AndroidChime;
import android.content.Context;


public class AndroidModule {

    private final Context context;

    public AndroidModule(Context context) {
        this.context = context;
    }

    public Chime getChime() {
        return new AndroidChime(context);
    }

    public Metronome getMetronome() {
        return new AndroidMetronome(context);
    }
}
