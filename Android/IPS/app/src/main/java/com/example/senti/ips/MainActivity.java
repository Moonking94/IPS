package com.example.senti.ips;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener  {

    private TextView txtResult, txtWifiInfoResult, txtWifiStateResult, txtIsWifiEnabledResult;
    private Button btnStart, btnStop;

    private volatile boolean ipsScan;
    private List<RouterInfo> listRI;

    private Thread t;

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

    private void start() {

        listRI = new LinkedList<>();
        ipsScan = true;

        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE);
        int state = wifi.getWifiState();
        WifiInfo info = wifi.getConnectionInfo(); // Get the connected wifi info

        wifi.startScan();
        List<ScanResult> result = wifi.getScanResults();

        txtWifiInfoResult.setText("Wifi info: " + info);
        txtWifiStateResult.setText("Wifi state: " + state);
        txtIsWifiEnabledResult.setText("Is Wifi Enabled ? : " + wifi.isWifiEnabled());
        System.out.println("Scan result: " + result);
        System.out.println(wifi.isScanAlwaysAvailable());

        txtResult.setText("Result\n");// Reset text view

        for(int i = 0;i<result.size();i++) {
            String ssid = result.get(i).SSID;
            String bssid = result.get(i).BSSID;
            double wifiDb = result.get(i).level;
            double frequency = result.get(i).frequency;
            double distance = calculateDistance(wifiDb, frequency);

            txtResult.append("SSID: " + ssid + "\n");
            txtResult.append("BSSID: " + bssid + "\n");
            txtResult.append("Level: " + wifiDb + "\n");
            txtResult.append("Frequency: " + frequency + "\n");
            txtResult.append("Distance: " + distance + " meter\n\n");

            RouterInfo ri = new RouterInfo(ssid, bssid, frequency, wifiDb);
            listRI.add(ri);
        }

        sendCoordinate();

//        t = new Thread() {
//
//            @Override
//            public void run() {
//                try {
//                    while (ipsScan) {
//                        Thread.sleep(1000);
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//
//                            }
//                        });
//                    }
//                } catch (InterruptedException e) {
//                    System.out.println(e.toString());
//                }
//            }
//        };

        //t.start();
    }

    private void stop() {
        ipsScan = false;
        txtWifiInfoResult.setText("");
        txtWifiStateResult.setText("");
        txtIsWifiEnabledResult.setText("");
        txtResult.setText("");
    }

//    private void saveData() {
//        String url = getApplicationContext().getString(R.string.raspberrypi_address) + getApplicationContext().getString(R.string.saveCurrentPosition);
//
//        StringRequest postRequest = new StringRequest(
//                Request.Method.POST,
//                url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        try {
//
//                            JSONObject jsonResponse = new JSONObject(response);
//                            boolean error = jsonResponse.getBoolean("responseError");
//
//                            if (!error) {
//                                Toast.makeText(getApplicationContext(), jsonResponse.getString("responseMsg"),
//                                        Toast.LENGTH_LONG).show();
//                            } else {
//                                Toast.makeText(getApplicationContext(), jsonResponse.getString("responseMsg"),
//                                        Toast.LENGTH_LONG).show();
//                            }
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                            Toast.makeText(getApplicationContext(), "JSON Error: " + e.getMessage(),
//                                    Toast.LENGTH_LONG).show();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//
//            // Handle and prompt if there are any connection error
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.e("MainActivity", "Save Error: " + error.getMessage());
//                if(error instanceof NoConnectionError) {
//                    Toast.makeText(getApplicationContext(), "Save failed:" + "Unable to connect to the server", Toast.LENGTH_LONG).show();
//                    //alert.showAlertDialog(LoginActivity.this, "Login failed", "Error connecting to the server", false);
//                } else if (error instanceof TimeoutError) {
//                    Toast.makeText(getApplicationContext(), "Save failed: " + "Connection time out", Toast.LENGTH_LONG).show();
//                    //alert.showAlertDialog(LoginActivity.this, "Login failed", "Connection time out", false);
//                }
//            }
//        }) {
//            /**
//             * Use to POST the parameter to the server
//             * @return - return variable contains the user credentials
//             */
//            @Override
//            protected Map<String, String> getParams() {
//
//                Map<String, String> params = new HashMap<>();
//
//                params.put("xaxis", xaxis);
//                params.put("yaxis", yaxis);
//                params.put("zaxis", zaxis);
//                params.put("userId", userId);
//
//                return params;
//            }
//        };
//
//        RequestQueue queue = Volley.newRequestQueue(this);
//        queue.add(postRequest);
//    }

    private void sendCoordinate() {
        String url = getApplicationContext().getString(R.string.raspberrypi_address) + getApplicationContext().getString(R.string.findCoordinate);

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
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), jsonResponse.getString("responseMsg"),
                                        Toast.LENGTH_LONG).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            System.out.println("ERROR: " + e.getMessage());
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
                    //alert.showAlertDialog(LoginActivity.this, "Login failed", "Error connecting to the server", false);
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(getApplicationContext(), "Save failed: " + "Connection time out", Toast.LENGTH_LONG).show();
                    //alert.showAlertDialog(LoginActivity.this, "Login failed", "Connection time out", false);
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

                // Need to do logic on which router to choose

                // For testing purposes
                for(int i = 0;i<listRI.size();i++) {
                    if(listRI.get(i).getSsid().equals("LenovoS650")) {
                        params.put("p1[0]", listRI.get(i).getSsid());
                        params.put("p1[1]", listRI.get(i).getFrequency() + "");
                        params.put("p1[2]", listRI.get(i).getSignalLvl() + "");
                    } else if(listRI.get(i).getSsid().equals("Jycans")) {
                        params.put("p2[0]", listRI.get(i).getSsid());
                        params.put("p2[1]", listRI.get(i).getFrequency() + "");
                        params.put("p2[2]", listRI.get(i).getSignalLvl() + "");
                    } else if(listRI.get(i).getSsid().equals("SMEAP01")) {
                        params.put("p3[0]", listRI.get(i).getSsid());
                        params.put("p3[1]", listRI.get(i).getFrequency() + "");
                        params.put("p3[2]", listRI.get(i).getSignalLvl() + "");
                    }
                }

                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(postRequest);
    }

    @Override
    public void onClick(View v) {
        if (v == btnStart) {
            start();
        }
        if(v == btnStop) {
            stop();
        }
    }

    private double calculateDistance(double signalLevelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }
}
