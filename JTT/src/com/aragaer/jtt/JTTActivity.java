package com.aragaer.jtt;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;

import java.util.TimeZone;

public class JTTActivity extends Activity {
	private JTT calculator;
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jtt);
    	float latitude, longitude;
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	latitude = Float.parseFloat(settings.getString("posLat", "0.0"));
    	longitude = Float.parseFloat(settings.getString("posLong", "0.0"));
        
    	calculator = new JTT(latitude, longitude, TimeZone.getDefault());
    }
}
