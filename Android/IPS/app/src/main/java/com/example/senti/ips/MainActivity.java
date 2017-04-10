package com.example.senti.ips;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener  {

    private TextView txtResult, txtWifiInfoResult, txtWifiStateResult, txtIsWifiEnabledResult;
    private Button btnStart, btnStop;

    private volatile boolean sendAllow=true;
    private List<RouterInfo> listRI;

    private Handler h = new Handler();
    private Runnable runAvg, runDbm;

    private ArrayList<Double> p1Arr = new ArrayList<>();
    private ArrayList<Double> p2Arr = new ArrayList<>();
    private ArrayList<Double> p3Arr = new ArrayList<>();
    private ArrayList<Double> p4Arr = new ArrayList<>();
    private ArrayList<Double> listAvgDbm = new ArrayList<>();

    // milliseconds
    private int totalDelay = 80;
    private int avgDelay = 2000;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtWifiInfoResult = (TextView)findViewById(R.id.txtWifiInfoResult);
        txtWifiStateResult = (TextView)findViewById(R.id.txtWifiStateResult);
        txtIsWifiEnabledResult = (TextView)findViewById(R.id.txtIsWifiEnabledResult);

        txtResult = (TextView)findViewById(R.id.txtView);
        btnStart = (Button)findViewById(R.id.btnStart);
        btnStop = (Button)findViewById(R.id.btnStop);

        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
    }

    private void stop() {
        txtWifiInfoResult.setText("");
        txtWifiStateResult.setText("");
        txtIsWifiEnabledResult.setText("");
        txtResult.setText("");
        h.removeCallbacks(runAvg);
        h.removeCallbacks(runDbm);
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
                Log.e("MainActivity", "Save Error: " + error.getMessage());
                if(error instanceof NoConnectionError) {
                    Toast.makeText(getApplicationContext(), "Save failed:" + "Unable to connect to the server", Toast.LENGTH_LONG).show();
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(getApplicationContext(), "Save failed: " + "Connection time out", Toast.LENGTH_LONG).show();
                }
            }
        }) {
            /**
             * Use to POST the parameter to the server
             * @return - return variable contains the user credentials
             */
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<>();
                String userLogged = "1";
                Boolean status = false;

                // Need to do logic on which router to choose *Choose router with the highest dbm reading

                // For testing purposes
                if(listRI.size() > 0 && !listRI.isEmpty()) {
                    for (int i = 0; i < listRI.size(); i++) {
                        Log.d(TAG, "SSID: " + listRI.get(i).getSsid());
                        Log.d(TAG, "BSSID: " + listRI.get(i).getBssid());
                        Log.d(TAG, "BSSID: " + listRI.get(i).getFrequency());

                        if(listRI.get(i).getBssid().equals("b8:27:eb:06:43:df")) { // PiAP 18:a6:f7:0d:51:04
                            params.put("p1[0]", listRI.get(i).getSsid());
                            params.put("p1[1]", listRI.get(i).getBssid());
//                            params.put("p1[2]", listRI.get(i).getSignalLvl() + "");
                            params.put("p1[2]", listAvgDbm.get(0) + "");
                            params.put("p1[3]", listRI.get(i).getLevel() + "");
                        } else if (listRI.get(i).getBssid().equals("b8:27:eb:1b:c5:23")) { // MoonPiAP b8:27:eb:1b:c5:23
                            params.put("p2[0]", listRI.get(i).getSsid());
                            params.put("p2[1]", listRI.get(i).getBssid());
//                            params.put("p1[2]", listRI.get(i).getSignalLvl() + "");
                            params.put("p2[2]", listAvgDbm.get(2) + "");
                            params.put("p2[3]", listRI.get(i).getLevel() + "");
                        } else if(listRI.get(i).getBssid().equals("c8:d3:a3:dd:8e:10") && status) { // nana_ian c8:d3:a3:dd:8e:10
                            params.put("p2[0]", listRI.get(i).getSsid());
                            params.put("p2[1]", listRI.get(i).getBssid());
                            params.put("p2[2]", listRI.get(i).getSignalLvl() + "");
                        } else if(listRI.get(i).getBssid().equals("d8:5d:4c:db:bb:f8") && status) { // TP-LINK_DBBBF8 b8:27:eb:1b:c5:23
                            params.put("p3[0]", listRI.get(i).getSsid());
                            params.put("p3[1]", listRI.get(i).getBssid());
                            params.put("p3[2]", listRI.get(i).getSignalLvl() + "");
                        } else if (listRI.get(i).getBssid().equals("b8:27:eb:81:20:99")) { // JeffPi b8:27:eb:81:20:99
                            params.put("p3[0]", listRI.get(i).getSsid());
                            params.put("p3[1]", listRI.get(i).getBssid());
//                            params.put("p3[2]", listRI.get(i).getSignalLvl() + "");
                            params.put("p3[2]", listAvgDbm.get(1) + "");
                            params.put("p3[3]", listRI.get(i).getLevel() + "");
                        } else if (listRI.get(i).getBssid().equals("e8:50:8b:07:43:fb") && status) { // Jycans BSSID (Jeffrey)
                            params.put("p2[0]", listRI.get(i).getSsid());
                            params.put("p2[1]", listRI.get(i).getBssid());
//                            params.put("p2[2]", listRI.get(i).getSignalLvl() + "");
                            params.put("p2[2]", listAvgDbm.get(1) + "");
                            params.put("p2[3]", listRI.get(i).getLevel() + "");
                        } else if (listRI.get(i).getBssid().equals("38:a4:ed:68:02:e5") && status) { // RedMi (Louis)
                            params.put("p4[0]", listRI.get(i).getSsid());
                            params.put("p4[1]", listRI.get(i).getBssid());
//                            params.put("p3[2]", listRI.get(i).getSignalLvl() + "");
                            params.put("p4[2]", listAvgDbm.get(0) + "");
                            params.put("p4[3]", listRI.get(i).getLevel() + "");
                        } else if (listRI.get(i).getBssid().equals("6e:5f:1c:d5:c8:b1")) { // LenovoS650 BSSID
                            params.put("p4[0]", listRI.get(i).getSsid());
                            params.put("p4[1]", listRI.get(i).getBssid());
//                            params.put("p3[2]", listRI.get(i).getSignalLvl() + "");
                            params.put("p4[2]", listAvgDbm.get(0) + "");
                            params.put("p4[3]", listRI.get(i).getLevel() + "");
                        } else if(listRI.get(i).getBssid().equals("12:2a:b3:a7:38:ee") && status) { // Jacky
                            params.put("p3[0]", listRI.get(i).getSsid());
                            params.put("p3[1]", listRI.get(i).getBssid() + "");
                            params.put("p3[2]", listRI.get(i).getSignalLvl() + "");
                        } else if (listRI.get(i).getBssid().equals("90:ef:68:c1:20:d6") && status) { // SMEAP01 BSSID(2462Mhz/2.462Ghz)
                            params.put("p3[0]", listRI.get(i).getSsid());
                            params.put("p3[1]", listRI.get(i).getBssid() + "");
                            params.put("p3[2]", listRI.get(i).getSignalLvl() + "");
                        }
                    }
                    params.put("userId", userLogged);
                }

                listAvgDbm.clear();
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(postRequest);
    }

    @Override
    public void onClick(View v) {
        if (v == btnStart) {
            getDbm();
            getAvg();
        }
        if(v == btnStop) {
            stop();
        }
    }

    /**
     * Notes:
     * distance = ToA ∗ c; c=3x10^5
     *
     * @param signalLevelInDb
     * @param freqInMHz
     * @return
     */
    private double calculateDistance(double signalLevelInDb, double freqInMHz) {
        double exp = (27.55d - (20d * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
//        double exp = (27.55d - (40d * Math.log10(freqInMHz)) + 6.7d - signalLevelInDb) / 20.0d; // We can see that values up to -50 dBm, can be used for somewhat relevant calculation. Lower values are highly unstable.
//        double exp = (Math.abs(signalLevelInDb) - 40)/(10*2);
        return Math.pow(10.0, exp);
    }

    /**
     * 𝑅𝑆𝑆𝐼 = − 10𝑛 𝑙𝑜𝑔10 𝑑 + 𝐴
     * n = -((RSSI - A)/(10(log10 x d)))
     * Variable:
     * A; where A = A is the received signal strength at 1 meter distance, A is obtained in a no-obstacle one-meter distance signal strength measurements from the reference nodes.
     * d; where d = d is the distance from sender;
     * n; where n = 2(Free space), 2.7~3.5(Urban area Cellular radio), 1.6~1.8(In Building LOS), 3~5(Shadowed Urban Area cellular radio)
     *
     *
     */

    private void getAvg() {
        runAvg = new Runnable() {
            @Override
            public void run(){
                ArrayList<ArrayList<Double>> listOfTotal = new ArrayList<>();
                listOfTotal.add(p1Arr);
                listOfTotal.add(p2Arr);
                listOfTotal.add(p3Arr);
                listOfTotal.add(p4Arr);

                for(int i=0;i<listOfTotal.size();i++) {
                    Double total = totalCalculation(listOfTotal.get(i));
                    listAvgDbm.add(total/listOfTotal.get(i).size());
                    Log.d("MainActivity", "Average for " + total + "/" + listOfTotal.get(i).size() + " is " + listAvgDbm.get(i) + ", The distance is : " + calculateDistance(listAvgDbm.get(i), 2437) + "m.");
                    listOfTotal.get(i).clear();
                }
                listOfTotal.clear();

                if(sendAllow) {
                    sendCoordinate();
                }

                h.postDelayed(this, avgDelay);
            }
        };

        h.postDelayed(runAvg, avgDelay);
    }

    private void getDbm() {
        runDbm = new Runnable() {
            @Override
            public void run() {
                listRI = new LinkedList<>();

                WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE);
                int state = wifiMgr.getWifiState();
                WifiInfo info = wifiMgr.getConnectionInfo(); // Get the connected wifiMgr info
                wifiMgr.startScan();
                List<ScanResult> result = wifiMgr.getScanResults();

                txtWifiInfoResult.setText("Wifi info: " + info);
                txtWifiStateResult.setText("Wifi state: " + state);
                Log.d(TAG, "Scan result: " + result);
                Log.d(TAG, wifiMgr.isScanAlwaysAvailable() + "");

                txtResult.setText("Result\n");// Reset text view

                for(int i = 0;i<result.size();i++) {
                    String ssid = result.get(i).SSID;
                    String bssid = result.get(i).BSSID;
                    double wifiDb = result.get(i).level;
                    int level = wifiMgr.calculateSignalLevel((int)wifiDb, 10);
                    double frequency = result.get(i).frequency;
                    double distance = calculateDistance(wifiDb, frequency);

                    if(ssid.equals("Redmi222") || ssid.equals("Jycans") || ssid.equals("PiAP") || ssid.equals("PiJeff") || ssid.equals("MoonPiAP") || ssid.equals("LenovoS650")) {
                        txtResult.append("SSID: " + ssid + "\n");
                        txtResult.append("BSSID: " + bssid + "\n");
                        txtResult.append("Level: " + wifiDb + "\n");
                        txtResult.append("Frequency: " + frequency + "\n");
                        txtResult.append("Signal Level: " + level + "\n");
                        txtResult.append("Timestamps: " + result.get(i).timestamp + "\n");
                        txtResult.append("Distance: " + new DecimalFormat("#.##").format(distance) + " m\n\n");

                        Log.d("MainActivity", new DecimalFormat("#.##").format(distance) + " m");

                        RouterInfo ri = new RouterInfo(ssid, bssid, frequency, wifiDb, level);
                        listRI.add(ri);

                        if(ssid.equals("PiAP")) {
                            p1Arr.add(wifiDb);
                            Log.d("MainActivity", "Wifi dbm for PiAP : " + wifiDb + "");
                        } else if(ssid.equals("PiJeff")) {
                            p2Arr.add(wifiDb);
                            Log.d("MainActivity", "Wifi dbm for PiJeff : " + wifiDb + "");
                        } else if(ssid.equals("MoonPiAP")){
                            p3Arr.add(wifiDb);
                            Log.d("MainActivity", "Wifi dbm for MoonPiAP : " + wifiDb + "");
                        } else {
                            p4Arr.add(wifiDb);
                            Log.d("MainActivity", "Wifi dbm for S650 : " + wifiDb + "");
                        }
                    }
                }

                h.postDelayed(this, totalDelay);
            }
        };

        h.postDelayed(runDbm, totalDelay);
    }

    private Double totalCalculation(ArrayList<Double> list) {
        Double total = 0.0;

        for(int i=0;i<list.size();i++) {
            total += list.get(i);
        }
        Log.d("MainActivity", "Total : " + total + "");

        return total;
    }
}
