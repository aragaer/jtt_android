package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4


public interface ComponentFactory {
    public Chime getChime();
    public Astrolabe getAstrolabe();
    public Metronome getMetronome();
}
