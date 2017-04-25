package com.example.senti.ips;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Senti on 4/25/2017.
 */

public class WiFiScanReceiver extends BroadcastReceiver {
    private static final String TAG = "WiFiScanReceiver";
    MainActivity wifiDemo;

    public WiFiScanReceiver(MainActivity wifiDemo) {
        super();
        this.wifiDemo = wifiDemo;
    }

    @Override
    public void onReceive(Context c, Intent intent) {
        
    }
}
