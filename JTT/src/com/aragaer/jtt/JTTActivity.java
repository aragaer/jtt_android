package com.aragaer.jtt;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;

import java.util.Date;
import java.util.TimeZone;

public class JTTActivity extends Activity {
	private JTT calculator;
	private final Runnable mUpdateUITimerTask = new Runnable() {
	    public void run() {
	    	JTTClockView hh = (JTTClockView) findViewById(R.id.hour);
	    	float latitude, longitude;
	    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	    	latitude = Float.parseFloat(settings.getString("posLat", "0.0"));
	    	longitude = Float.parseFloat(settings.getString("posLong", "0.0"));
	        
	    	calculator = new JTT(latitude, longitude, TimeZone.getDefault());
	    	
	    	JTTHour hour = calculator.time_to_jtt(new Date());
	    	
	    	hh.setJTTHour(hour);
	    }
	};
	private final Handler mHandler = new Handler();
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jtt);
        mHandler.postDelayed(mUpdateUITimerTask, 100);
    }
}
