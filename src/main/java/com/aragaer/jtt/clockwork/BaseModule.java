package com.aragaer.jtt.clockwork;
// vim: et ts=4 sts=4 sw=4

import dagger.*;
import javax.inject.Singleton;

@Module(injects={Cogs.class})
public class BaseModule {

    @Provides @Singleton Chime getChime() {
        return null;
    }

}
