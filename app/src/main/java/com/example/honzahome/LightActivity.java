package com.example.honzahome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.mollin.yapi.YeelightDevice;
import com.mollin.yapi.enumeration.YeelightEffect;
import com.mollin.yapi.enumeration.YeelightProperty;
import com.mollin.yapi.exception.YeelightResultErrorException;
import com.mollin.yapi.exception.YeelightSocketException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class LightActivity extends AppCompatActivity {

    /* MAIN */
    TextView stateB, stateC;
    Button button, btnSel, btnClose, btnSearch, btnPrgClose;
    YeelightDevice device;
    SeekBar bright, color;
    WifiManager mainWifi;
    PopupWindow popupWindow;
    SharedPreferences settings;
    String ip;
    Integer prog;
    RelativeLayout progressLay;
    ProgressBar progressBar;
    Boolean connected;
    /* /MAIN */

    /* SEARCH */
    private static final String TAG = "APITEST";
    private static final int MSG_SHOWLOG = 0;
    private static final int MSG_FOUND_DEVICE = 1;
    private static final int MSG_DISCOVER_FINISH = 2;
    private static final int MSG_STOP_SEARCH = 3;

    private static final String UDP_HOST = "239.255.255.250";
    private static final int UDP_PORT = 1982;
    private static final String message = "M-SEARCH * HTTP/1.1\r\n" +
            "HOST:239.255.255.250:1982\r\n" +
            "MAN:\"ssdp:discover\"\r\n" +
            "ST:wifi_bulb\r\n";//用于发送的字符串
    private DatagramSocket mDSocket;
    private boolean mSeraching = true;
    private ListView mListView;
    private MyAdapter mAdapter;
    List<HashMap<String, String>> mDeviceList = new ArrayList<HashMap<String, String>>();
    private Button mBtnSearch;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_FOUND_DEVICE:
                    mAdapter.notifyDataSetChanged();
                    break;
                case MSG_SHOWLOG:
                    Toast.makeText(LightActivity.this, "" + msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case MSG_STOP_SEARCH:
                    mSearchThread.interrupt();
                    mAdapter.notifyDataSetChanged();
                    mSeraching = false;
                    break;
                case MSG_DISCOVER_FINISH:
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    private WifiManager.MulticastLock multicastLock;
    /* /SEARCH */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);

        connected = false;

        stateB = findViewById(R.id.txtStateBval);
        stateC = findViewById(R.id.txtStateCval);
        button = findViewById(R.id.btnOnOff);
        btnPrgClose = findViewById(R.id.btnPrgClose);
        bright = findViewById(R.id.seekBright);
        color = findViewById(R.id.seekColor);
        btnSel  = findViewById(R.id.btnSel);
        progressLay = findViewById(R.id.progressLay);
        progressBar = findViewById(R.id.progressBar);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            /*
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setTitle("Title");
            builder.setMessage("Message");
            builder.setPositiveButton("Confirm",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
             */
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        //192.168.1.173
        ip = settings.getString("ip","").toString();

        if (ip == "") {
            progressLay.setVisibility(View.INVISIBLE);
        } else {
            progressLay.setVisibility(View.VISIBLE);
            btnPrgClose.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    progressLay.setVisibility(View.INVISIBLE);
                }
            });
        }

        if (checkWIFI() == false) {
            mainWifi.setWifiEnabled(true);
        }

        button.setOnClickListener(v -> {

            Thread thread = new Thread(() -> {
                try{
                    changeDevice(ip);
                    /*int brightness = Integer.valueOf(device.getProperties(BRIGHTNESS).values().iterator().next());
                    int colortemp = Integer.valueOf(device.getProperties(COLOR_TEMPERATURE).values().iterator().next());
                    String pow = device.getProperties(POWER).values().iterator().next();

                    if (pow.equals("on")) {

                    } else {
                        color.setProgress(colortemp - 2700);
                        bright.setProgress(brightness);
                    }*/
                    device.toggle();
                } catch(NoClassDefFoundError e){
                    System.out.println("Error " + e.getMessage());
                } catch (YeelightSocketException e) {
                    System.out.println("Error " + e.getMessage());
                } catch (YeelightResultErrorException e) {
                    System.out.println("Error " + e.getMessage());
                }  catch(Exception e){
                    System.out.println("Error " + e.getMessage());
                }
            });
            thread.start();
        });

        btnSel.setOnClickListener(v -> {
            LayoutInflater inflater = (LayoutInflater)
                    getSystemService(LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.popup_window,
                    (ViewGroup) LightActivity.this.findViewById(R.id.popup_element));

            int width = LinearLayout.LayoutParams.MATCH_PARENT;
            int height = LinearLayout.LayoutParams.MATCH_PARENT;
            boolean focusable = true; // lets taps outside the popup also dismiss it
            popupWindow = new PopupWindow(layout, width, height, focusable);

            popupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);

            btnClose = layout.findViewById(R.id.btnClose);
            btnSearch = layout.findViewById(R.id.btnSearch);
            mListView = layout.findViewById(R.id.deviceList);

            mAdapter = new MyAdapter(this);
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    HashMap<String, String> bulbInfo = mDeviceList.get(position);
                    String ipinfo = bulbInfo.get("Location").replaceAll(" ","");
                    Log.d(TAG, "lcheck = " + ipinfo);
                    String new_ip = ipinfo.split(":")[0];
                    String new_port = ipinfo.split(":")[1];

                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("ip", new_ip);
                    editor.commit();

                    ip = settings.getString("ip","").toString();

                    mDeviceList.clear();

                    changeDevice(ip);

                    onClickedClose();
                }
            });

            WifiManager wm = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            multicastLock = wm.createMulticastLock("test");
            multicastLock.acquire();

            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickedClose();
                }
            });

            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchDevice();
                }

            });
        });

        bright.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int valueB = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressB, boolean fromUser) {
                valueB = progressB;
                if (valueB == 0) {
                    valueB = progressB + 1;
                    seekBar.setProgress(1);
                    stateB.setText(Integer.toString(valueB));
                } else {
                    valueB = progressB;
                    stateB.setText(Integer.toString(valueB));
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Thread thread = new Thread(() -> {
                    try{
                        device.setBrightness(valueB);
                        stateB.setText(Integer.toString(valueB));
                        stateB.setTextColor(Color.GREEN);
                    } catch(NoClassDefFoundError e){
                        System.out.println("Error " + e.getMessage());
                    } catch(Exception e){
                        System.out.println("Error " + e.getMessage());
                    }
                });
                thread.start();
            }
        });

        color.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int valueC = 0;
            int minC = 2700;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressC, boolean fromUser) {
                valueC = progressC + minC;

                stateC.setText(Integer.toString(valueC));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Thread thread = new Thread(() -> {
                    try{
                        device.setColorTemperature(valueC);
                        stateC.setText(Integer.toString(valueC));
                        stateC.setTextColor(Color.GREEN);
                    } catch(NoClassDefFoundError e){
                        System.out.println("Error " + e.getMessage());
                    } catch(Exception e){
                        System.out.println("Error " + e.getMessage());
                    }
                });

                thread.start();
            }
        });

        prog = 1;

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                while (connected == false) {
                    try {
                        Thread.sleep(500); // Waits for 1 second (1000 milliseconds)
                        changeDevice(ip);

                        if (prog > 100) {
                            prog = prog - 100;
                        }

                        progressBar.setProgress(prog);
                        prog = prog + 1;

                        System.out.println("properties: " + device.toString());
                        System.out.println("properties: " + device.getClass().toString());

                        //int brightness = Integer.valueOf(device.getProperties(BRIGHTNESS).values().iterator().next());
                        //int colortemp = Integer.valueOf(device.getProperties(COLOR_TEMPERATURE).values().iterator().next());
                        //System.out.println("brightness: " + device.getProperties());
                        //System.out.println("colortemp: " + device.getProperties());
                        System.out.println("1: " + YeelightProperty.values());
                        System.out.println("2: " + YeelightProperty.valueOf("POWER").getValue());

                        color.setProgress(5700 - 2700);
                        bright.setProgress(50);
                        progressLay.setVisibility(View.INVISIBLE);

                        connected = true;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch(NoClassDefFoundError e){
                        System.out.println("Error " + e.getMessage());
                    }  catch(Exception e){
                        System.out.println("Error " + e.getMessage());
                    }
                }
            };
        };

        Thread myThread = new Thread(myRunnable);
        myThread.start();

    }

    /* MAIN */

    public void changeDevice(String ip){
        Thread thread = new Thread(() -> {
            try{
                device = new YeelightDevice(ip, 55443, YeelightEffect.SMOOTH, 500);
            }
            catch(Exception e){
                System.out.println("Error " + e.getMessage());
            }
        });
        thread.start();
    }

    @Override
    protected void onDestroy() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mainWifi.setWifiEnabled(false);
        super.onDestroy();
    }

    public void onClickedClose() {
        popupWindow.dismiss();
        multicastLock.release();
    }

    public Boolean checkWIFI() {
        Boolean stateWIFI = false;

        mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mainWifi.isWifiEnabled()) {
            stateWIFI = true;
        }
        return stateWIFI;
    }
    /* /MAIN */
    /* SEARCH */


    private Thread mSearchThread = null;
    private void searchDevice() {

        mDeviceList.clear();
        mAdapter.notifyDataSetChanged();
        mSeraching = true;
        mSearchThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mDSocket = new DatagramSocket();
                    DatagramPacket dpSend = new DatagramPacket(message.getBytes(),
                            message.getBytes().length, InetAddress.getByName(UDP_HOST),
                            UDP_PORT);
                    mDSocket.send(dpSend);
                    mHandler.sendEmptyMessageDelayed(MSG_STOP_SEARCH,2000);
                    while (mSeraching) {
                        byte[] buf = new byte[1024];
                        DatagramPacket dpRecv = new DatagramPacket(buf, buf.length);
                        mDSocket.receive(dpRecv);
                        byte[] bytes = dpRecv.getData();
                        StringBuffer buffer = new StringBuffer();
                        for (int i = 0; i < dpRecv.getLength(); i++) {
                            // parse /r
                            if (bytes[i] == 13) {
                                continue;
                            }
                            buffer.append((char) bytes[i]);
                        }
                        Log.d("socket", "got message:" + buffer.toString());
                        if (!buffer.toString().contains("yeelight")) {
                            mHandler.obtainMessage(MSG_SHOWLOG, "收到一条消息,不是Yeelight灯泡").sendToTarget();
                            return;
                        }
                        String[] infos = buffer.toString().split("\n");

                        HashMap<String, String> bulbInfo = new HashMap<String, String>();
                        for (String str : infos) {
                            int index = str.indexOf(":");
                            if (index == -1) {
                                continue;
                            }
                            String title = str.substring(0, index);
                            String value = str.substring(index + 1);

                            value = value.replace("yeelight://", "");

                            bulbInfo.put(title, value);
                        }
                        if (!hasAdd(bulbInfo)){
                            mDeviceList.add(bulbInfo);
                        }

                    }
                    mHandler.sendEmptyMessage(MSG_DISCOVER_FINISH);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mSearchThread.start();
    }

    private class MyAdapter extends BaseAdapter {

        private LayoutInflater mLayoutInflater;
        private int mLayoutResource;

        public MyAdapter(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
            mLayoutResource = android.R.layout.simple_list_item_2;
        }

        @Override
        public int getCount() {
            return mDeviceList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDeviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            HashMap<String, String> data = (HashMap<String, String>) getItem(position);
            if (convertView == null) {
                view = mLayoutInflater.inflate(mLayoutResource, parent, false);
            } else {
                view = convertView;
            }
            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText("Model: "+data.get("model") );

            Log.d(TAG, "name = " + textView.getText().toString());
            TextView textSub = (TextView) view.findViewById(android.R.id.text2);
            textSub.setText("IP: " + data.get("Location"));
            return view;
        }
    }
    private boolean hasAdd(HashMap<String,String> bulbinfo){
        for (HashMap<String,String> info : mDeviceList){
            Log.d(TAG, "location params = " + bulbinfo.get("Location"));
            if (info.get("Location").equals(bulbinfo.get("Location"))){
                return true;
            }
        }
        return false;
    }

    /* /SEARCH */
}