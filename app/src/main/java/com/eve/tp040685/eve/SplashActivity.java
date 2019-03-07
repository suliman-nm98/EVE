package com.eve.tp040685.eve;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    private final int STR_SPLASH_TIME = 2000;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            startSplashTimer();
            setContentView(R.layout.activity_splash);
        }

        private void startSplashTimer() {
            try {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, STR_SPLASH_TIME);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
}
