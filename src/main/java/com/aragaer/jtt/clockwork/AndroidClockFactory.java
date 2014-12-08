package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import android.content.Context;

import com.aragaer.jtt.astronomy.DayIntervalCalculator;
import com.aragaer.jtt.astronomy.SscCalculator;
import com.aragaer.jtt.location.AndroidLocationProvider;
import com.aragaer.jtt.location.LocationProvider;


public class AndroidClockFactory implements ComponentFactory {

    private final Astrolabe astrolabe;
    private final Chime chime;
    private final Metronome metronome;

    public AndroidClockFactory(Context context) {
        LocationProvider provider = new AndroidLocationProvider(context);
        DayIntervalCalculator calculator = new SscCalculator();
        astrolabe = new Astrolabe(calculator, provider);
        chime = new Chime(context);
        metronome = new AndroidMetronome(context);
    }

    public Astrolabe getAstrolabe() {
        return astrolabe;
    }

    public Chime getChime() {
        return chime;
    }

    public Metronome getMetronome() {
        return metronome;
    }
}
