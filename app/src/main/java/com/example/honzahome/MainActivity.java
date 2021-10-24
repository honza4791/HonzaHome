package com.example.honzahome;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RequestSwitch.AsyncResponse {

    Button btnSwitch;
    TextView txtState, txtTemp, txtHum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        btnSwitch = findViewById(R.id.btn_switch);
        txtState = findViewById(R.id.txt_status_val);
        txtTemp = findViewById(R.id.txt_temp_val);
        txtHum = findViewById(R.id.txt_hum_val);

        btnSwitch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clicked();
            }
        });

        new RequestSwitch(this).execute("https://script.google.com/macros/s/AKfycbxbsHHoQrJKOCgqLcgWqnkMVGHO3zM6taS6Xg7-HVmse-0vY-bIB5257FtMAOoFU7S_/exec?z=l");
    }

    private void clicked() {
        new RequestSwitch(this).execute("https://script.google.com/macros/s/AKfycbxbsHHoQrJKOCgqLcgWqnkMVGHO3zM6taS6Xg7-HVmse-0vY-bIB5257FtMAOoFU7S_/exec?z=s");
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
            txtState.setText("ERROR");
        }
        txtTemp.setText(m.get(0) + " °C");
        txtHum.setText(m.get(1) + " %");
    }
}