package com.aragaer.ticker;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView.SavedState;

public class TickerTestActivity extends Activity {
    private final static String TAG = "ticker test"; 
    private Ticker ticker = new Ticker(5, 10) {
        @Override
        public int overrun() {
            Log.d(TAG, "overrun()");
            SystemClock.sleep(15000);
            long now = System.currentTimeMillis() - 12700;
            for (int i = 0; i < 5; i++)
                ticker.add_tr(now + i * 60000);
            return Ticker.KEEP_TICKING;
        }

        @Override
        public int underrun() {
            Log.d(TAG, "underrun()");
            return Ticker.STOP_TICKING;
        }

        @Override
        public void handle_tick(int tick, int sub) {
            Log.d(TAG, "handleTick("+tick+","+sub+")");
        }

        @Override
        public void handle_sub(int tick, int sub) {
            Log.d(TAG, "handleSub("+tick+","+sub+")");
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            long now = System.currentTimeMillis() - 15000;
            for (int i = 0; i < 5; i++)
                ticker.add_tr(now + i * 60000);
        } else {
            Log.d(TAG, "Loading");
            ticker.load_from_bundle(savedInstanceState, "ticker");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ticker.start_ticking();
        SystemClock.sleep(100);
        ticker.start_ticking();
    }
    
    @Override
    protected void onPause() {
        ticker.stop_ticking();
        super.onPause();
    }

    @Override
    protected void onResume() {
        ticker.start_ticking();
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "Saving");
        ticker.save_to_bundle(outState, "ticker");
        super.onSaveInstanceState(outState);
    }
}