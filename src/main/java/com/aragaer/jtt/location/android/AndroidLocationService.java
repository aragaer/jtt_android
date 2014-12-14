package com.aragaer.jtt.location;
// vim: et ts=4 sts=4 sw=4

import android.content.Context;


public class AndroidLocationService extends LocationService {
    public AndroidLocationService(Context context) {
        super(new AndroidLocationProvider(context), new AndroidLocationChangeNotifier(context));
    }
}
