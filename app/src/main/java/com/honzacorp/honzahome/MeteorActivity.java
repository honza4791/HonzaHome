package com.honzacorp.honzahome;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MeteorActivity extends AppCompatActivity implements GetRequestActivity.AsyncResponse {

    Button btnSwitch, btnUpdate;
    TextView txtState, txtTemp, txtHum;
    WifiManager mainWifi;
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 3*1000;
    String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meteor);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        key = getString(R.string.AppsScriptKey);

        if (checkWIFI() == false) {
            mainWifi.setWifiEnabled(true);
        }

        btnSwitch = findViewById(R.id.btn_switch);
        btnUpdate = findViewById(R.id.btn_update);
        txtState = findViewById(R.id.txt_status_val);
        txtTemp = findViewById(R.id.txt_temp_val);
        txtHum = findViewById(R.id.txt_hum_val);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clickedU();
            }
        });

        btnSwitch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clickedS();
            }
        });

        clickedU();
    }

    private void clickedU() {
        new GetRequestActivity(this).execute("https://script.google.com/macros/s/" + key + "/exec?z=l");
    }

    private void clickedS() {
        new GetRequestActivity(this).execute("https://script.google.com/macros/s/" + key + "/exec?z=s");
    }

    @Override
    public void processFinish(String output) {
        List<String> m = new ArrayList<String>(Arrays.asList(output.split(";")));
        String s = m.get(2);
        if (s.equals("1")) {
            txtState.setText("ON");
        } else if (s.equals("0")) {
            txtState.setText("OFF");
        } else {
            txtState.setText(s);
        }
        txtTemp.setText(m.get(0) + " °C");
        txtHum.setText(m.get(1) + " %");
    }

    @Override
    protected void onDestroy() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mainWifi.setWifiEnabled(false);
        super.onDestroy();
    }

    public Boolean checkWIFI() {
        Boolean stateWIFI = false;

        mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mainWifi.isWifiEnabled()) {
            stateWIFI = true;
        }
        return stateWIFI;
    }

    @Override
    protected void onResume() {

        handler.postDelayed( runnable = new Runnable() {
            public void run() {
                clickedU();
                handler.postDelayed(runnable, delay);
            }
        }, delay);
        super.onResume();
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }
}