package com.example.senti.ips;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends AppCompatActivity implements View.OnClickListener  {

    private Button btnStart, btnStop;
    private WebView wvUserLoc;

    private List<RouterInfo> listRI = new LinkedList<>();
    private List<RouterInfo> selRI = new LinkedList<>();

    private LinkedHashMap<String, ArrayList<Double>> linkmapDbm = new LinkedHashMap<>();

    // In Milliseconds
    private int totalDelay = 80;
    private int sendDelay = 2000;
    private int modeDelay = sendDelay;

    private Handler h = new Handler();
    private Runnable runSelectRSSI, runDbm;

    private volatile boolean sendAllow = false;

    private final static String TAG = "MainActivity";

    WifiManager wifiMgr;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> result = wifiMgr.getScanResults();
            for(int i = 0;i<result.size();i++) {
                String ssid = result.get(i).SSID;
                String bssid = result.get(i).BSSID;
                double frequency = result.get(i).frequency;
                double wifiDb = result.get(i).level;

                if(ssid.contains("IPS_AP") || ssid.contains("IPS-AP")) {
                    RouterInfo ri = new RouterInfo(ssid, bssid, frequency, wifiDb);

                    if(!linkmapDbm.containsKey(ri.getBssid())) {
                        listRI.add(ri);
                        ArrayList<Double> tempArr = new ArrayList<>();
                        tempArr.add(ri.getSignalLvl());

                        linkmapDbm.put(ri.getBssid(), tempArr);
                    } else {
                        ArrayList<Double> tempArr = linkmapDbm.get(ri.getBssid());
                        tempArr.add(ri.getSignalLvl());

                        linkmapDbm.put(ri.getBssid(), tempArr);
                    }
                }
            }
        }
    };// listens to the broadcasted wifi signals

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = (Button)findViewById(R.id.btnStart);
        btnStop = (Button)findViewById(R.id.btnStop);
        wvUserLoc = (WebView)findViewById(R.id.wvUserLoc);

        wifiMgr = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        webViewSetup();

        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btnStart) {
            registerReceiver(receiver, new IntentFilter(
                    WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            getDbm();
            selectRSSI();
        }
        if(v == btnStop) {
            stop();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            unregisterReceiver(receiver);
        }catch(IllegalArgumentException e) {
            Log.d(TAG, e.toString());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            unregisterReceiver(receiver);
        }catch(IllegalArgumentException e) {
            Log.d(TAG, e.toString());
        }
    }

    // Stop all the activity of WiFi Scan and clear the text.
    private void stop() {
        listRI.clear();
        linkmapDbm.clear();
        h.removeCallbacks(runDbm);
        h.removeCallbacks(runSelectRSSI);
        try {
            unregisterReceiver(receiver);
        }catch(IllegalArgumentException e) {
            Log.d(TAG, e.toString());
        }
    }

    private void webViewSetup() {

        //wvUserLoc.setInitialScale(1);
        WebSettings webSettings = wvUserLoc.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        wvUserLoc.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        wvUserLoc.setScrollbarFadingEnabled(false);

        wvUserLoc.loadUrl(getString(R.string.WebViewAddress) + "?userId=" + getUserId() + "&userLoc=" + getUserLoc());
    }

    @JavascriptInterface
    public Integer getUserId() { return 1; }

    @JavascriptInterface
    public String getUserLoc() {
        return "3A";
    }

    // Send current coordinate to server
    private void sendCoordinate() {
        String url = getApplicationContext().getString(R.string.raspberrypi_address) + getApplicationContext().getString(R.string.updatePosition);

        StringRequest postRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean error = jsonResponse.getBoolean("responseError");

                            if (!error) {
                                Toast.makeText(getApplicationContext(), jsonResponse.getString("responseMsg"),
                                        Toast.LENGTH_SHORT).show();
                                Log.d(TAG, ">>>>>>>>>>" + jsonResponse.getString("responseMsg") + "<<<<<<<<<<");
                            } else {
                                Toast.makeText(getApplicationContext(), jsonResponse.getString("responseMsg"),
                                        Toast.LENGTH_LONG).show();
                                Log.d(TAG, ">>>>>>>>>>" + jsonResponse.getString("responseMsg") + "<<<<<<<<<<");
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "ERROR: " + e.getMessage());
                            Toast.makeText(getApplicationContext(), "JSON Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {

            // Handle and prompt if there are any connection error
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Save Error: " + error.getMessage());
                if (error instanceof NoConnectionError) {
                    Toast.makeText(getApplicationContext(), "Save failed:" + "Unable to connect to the server", Toast.LENGTH_LONG).show();
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(getApplicationContext(), "Save failed: " + "Connection time out", Toast.LENGTH_LONG).show();
                }
            }
        }) {
            /**
             * Use to POST the parameter to the server
             *
             * @return - return variable contains the user credentials
             */
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<>();
                String userLogged = "1";

                if (selRI.size() > 0 && !selRI.isEmpty()) {
                    for (int i = 0; i < 3; i++) {
                        Log.d(TAG, "SSID: " + selRI.get(i).getSsid());
                        Log.d(TAG, "BSSID: " + selRI.get(i).getBssid());
                        Log.d(TAG, "Frequency: " + selRI.get(i).getFrequency());
                        Log.d(TAG, "Signal: " + selRI.get(i).getSignalLvl());

                        switch (i) {
                            case 0:
                                params.put("p1[0]", selRI.get(i).getSsid());
                                params.put("p1[1]", selRI.get(i).getBssid());
                                params.put("p1[2]", selRI.get(i).getSignalLvl() + "");
                                Log.d(TAG, "Point 1 added");
                                break;
                            case 1:
                                params.put("p2[0]", selRI.get(i).getSsid());
                                params.put("p2[1]", selRI.get(i).getBssid());
                                params.put("p2[2]", selRI.get(i).getSignalLvl() + "");
                                Log.d(TAG, "Point 2 added");
                                break;
                            case 2:
                                params.put("p3[0]", selRI.get(i).getSsid());
                                params.put("p3[1]", selRI.get(i).getBssid());
                                params.put("p3[2]", selRI.get(i).getSignalLvl() + "");
                                Log.d(TAG, "Point 3 added");
                                break;
                            default:
                                Log.d(TAG, "Error when selecting RI");
                                break;
                        }
                    }
                    params.put("userId", userLogged);
                }

                selRI = new LinkedList<>();
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        postRequest.setShouldCache(false);
        queue.add(postRequest);
    }

    private void selectRSSI() {
        runSelectRSSI = new Runnable() {
            @Override
            public void run() {
                if (listRI.size() > 0 && listRI.isEmpty())
                    Log.d(TAG, "There are no known access point !");

                for (int i = 0; i < listRI.size(); i++) {
                    RouterInfo ri = listRI.get(i);

                    if(selRI.isEmpty()) {
                        selRI.add(listRI.get(i));
                    } else {
                        boolean added = false;
                        for(int j=0;j<selRI.size();j++) {
                            if(ri.getSignalLvl() > selRI.get(j).getSignalLvl()) {
                                selRI.add(j, ri);
                                added = true;
                                break;
                            }
                        }

                        if(!added && selRI.size() < 3)
                            selRI.add(ri);
                    }
                }

                if(sendAllow) {
                    sendCoordinate();
                }

                listRI = new LinkedList<>();
                linkmapDbm.clear();

                h.postDelayed(this, modeDelay);
            }
        };
        h.postDelayed(runSelectRSSI, modeDelay);
    }

    private void getDbm() {
        runDbm = new Runnable() {
            @Override
            public void run() {
                wifiMgr.startScan();
                h.postDelayed(this, totalDelay);
            }
        };

        h.postDelayed(runDbm, totalDelay);
    }
}