package com.example.honzahome;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;

public class SplashscreenActivity extends AppCompatActivity {

    private static final int DELAY = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        new Handler().postDelayed(() -> {
            final Intent newIntent = new Intent(SplashscreenActivity.this, MainActivity.class);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(newIntent);
            finish();
        }, DELAY);
    }
}