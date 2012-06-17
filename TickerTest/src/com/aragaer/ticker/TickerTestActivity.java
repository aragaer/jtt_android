package com.aragaer.ticker;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class TickerTestActivity extends Activity {
    private final static String TAG = "ticker test"; 
    private Ticker ticker = new Ticker(5, 10) {
        @Override
        public void exhausted() {
            Log.d(TAG, "exhausted()");
        }

        @Override
        public void handleTick(int tick, int sub) {
            Log.d(TAG, "handleTick("+tick+","+sub+")");
        }

        @Override
        public void handleSub(int tick, int sub) {
            Log.d(TAG, "handleSub("+tick+","+sub+")");
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        long now = System.currentTimeMillis() - 15000;
        ticker.init(now);
        for (int i = 1; i < 10; i++)
            ticker.add_tr(now + i * 10000);
        ticker.start_ticking();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}