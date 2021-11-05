package com.example.honzahome;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class FeaturesActivity extends AppCompatActivity {

    Button btnTem, btnLig, btnSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_features);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        btnTem = findViewById(R.id.btnTemp);
        btnLig = findViewById(R.id.btnLight);
        btnSet = findViewById(R.id.btnSettings);

        btnTem.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(FeaturesActivity.this, TempActivity.class);
                startActivity(intent);
            }
        });

        btnLig.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(FeaturesActivity.this, LightActivity.class);
                startActivity(intent);
            }
        });

        btnSet.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(FeaturesActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }
}