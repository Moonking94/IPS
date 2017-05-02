package com.example.senti.ips;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends AppCompatActivity implements View.OnClickListener  {

    //private TextView txtResult, txtWifiInfoResult, txtWifiStateResult, txtIsWifiEnabledResult;
    private Button btnStart, btnStop;
    private WebView wvUserLoc;

    private List<RouterInfo> listRI = new LinkedList<>();
    private List<RouterInfo> selRI = new LinkedList<>();

    private LinkedHashMap<String, ArrayList<Double>> linkmapDbm = new LinkedHashMap<>();

    // In Milliseconds
    private int totalDelay = 80;
    private int sendDelay = 2000;
    private int modeDelay = sendDelay;
    private int avgDelay = sendDelay;

    private Handler h = new Handler();
    private Runnable runAvg, runMode, runKalman, runDbm;

    private volatile boolean sendAllow = false;

    private final static String TAG = "MainActivity";

    WifiManager wifiMgr;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> result = wifiMgr.getScanResults();
            //txtResult.setText("");
            for(int i = 0;i<result.size();i++) {
                String ssid = result.get(i).SSID;
                String bssid = result.get(i).BSSID;
                double wifiDb = result.get(i).level;
                int level = wifiMgr.calculateSignalLevel((int)wifiDb, 10);
                double frequency = result.get(i).frequency;
                double distance = calculateDistance(wifiDb, frequency);

                if(ssid.contains("IPS_AP") || ssid.contains("IPS-AP")) {
//                    txtResult.append("SSID: " + ssid + "\n");
//                    txtResult.append("BSSID: " + bssid + "\n");
//                    txtResult.append("Level: " + wifiDb + "\n");
//                    txtResult.append("Frequency: " + frequency + "\n");
//                    txtResult.append("Signal Level: " + level + "\n");
//                    txtResult.append("Distance: " + new DecimalFormat("#.##").format(distance) + " m\n\n");

                    Log.d(TAG, "Distance before average: " + new DecimalFormat("#.##").format(distance) + " m, RRSI : " + wifiDb);

                    RouterInfo ri = new RouterInfo(ssid, bssid, frequency, wifiDb, level);

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

//        txtWifiInfoResult = (TextView)findViewById(R.id.txtWifiInfoResult);
//        txtWifiStateResult = (TextView)findViewById(R.id.txtWifiStateResult);
//        txtIsWifiEnabledResult = (TextView)findViewById(R.id.txtIsWifiEnabledResult);

        //txtResult = (TextView)findViewById(R.id.txtView);
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
//            getAvg();
//            getMode();
            kalmanfilter();
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
//        txtWifiInfoResult.setText("");
//        txtWifiStateResult.setText("");
//        txtIsWifiEnabledResult.setText("");
//        txtResult.setText("");
        listRI.clear();
        linkmapDbm.clear();
        h.removeCallbacks(runAvg);
        h.removeCallbacks(runDbm);
        h.removeCallbacks(runKalman);
        try {
            unregisterReceiver(receiver);
        }catch(IllegalArgumentException e) {
            Log.d(TAG, e.toString());
        }
    }

    private void webViewSetup() {
        WebSettings webSettings = wvUserLoc.getSettings();
        webSettings.setJavaScriptEnabled(true);

        wvUserLoc.loadUrl(getString(R.string.WebViewAddress) + "?userId=" + getUserId() + "&userLoc=" + getUserLoc());
    }

    @JavascriptInterface
    public Integer getUserId() {
        return 1;
    }

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

                if(selRI.size() > 0 && !selRI.isEmpty()) {
                    for (int i = 0; i < 3; i++) {
                        Log.d(TAG, "SSID: " + selRI.get(i).getSsid());
                        Log.d(TAG, "BSSID: " + selRI.get(i).getBssid());
                        Log.d(TAG, "Frequency: " + selRI.get(i).getFrequency());
                        Log.d(TAG, "Signal: " + selRI.get(i).getSignalLvl());

                        switch(i){
                            case 0:
                                params.put("p1[0]", selRI.get(i).getSsid());
                                params.put("p1[1]", selRI.get(i).getBssid());
                                params.put("p1[2]", selRI.get(i).getSignalLvl() + "");
                                params.put("p1[3]", selRI.get(i).getLevel() + "");
                                Log.d(TAG, "Point 1 added");
                                break;
                            case 1:
                                params.put("p2[0]", selRI.get(i).getSsid());
                                params.put("p2[1]", selRI.get(i).getBssid());
                                params.put("p2[2]", selRI.get(i).getSignalLvl() + "");
                                params.put("p2[3]", selRI.get(i).getLevel() + "");
                                Log.d(TAG, "Point 2 added");
                                break;
                            case 2:
                                params.put("p3[0]", selRI.get(i).getSsid());
                                params.put("p3[1]", selRI.get(i).getBssid());
                                params.put("p3[2]", selRI.get(i).getSignalLvl() + "");
                                params.put("p3[3]", selRI.get(i).getLevel() + "");
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

    private void getAvg() {
        runAvg = new Runnable() {
            @Override
            public void run(){

                if(listRI.size() > 0 && listRI.isEmpty())
                    Log.d(TAG, "There are no known access point !");

                for(int i=0;i<listRI.size();i++) {
                    RouterInfo ri = listRI.get(i);

                    ArrayList<Double> tempArr = linkmapDbm.get(ri.getBssid());
                    Double total = totalCalculation(tempArr);
                    //Double avg = total/tempArr.size();
                    Double avg = Math.log10(total/(avgDelay/1000))*10;
                    ri.setSignalLvl(avg);

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

                    Log.d(TAG, "Average for " + total + "/" + tempArr.size() + " is " + avg + ", The distance after average is : " + new DecimalFormat("#.##").format(calculateDistance(avg, 2437)) + "m.");
                }

                if(sendAllow) {
                    sendCoordinate();
                }

                listRI = new LinkedList<>();
                linkmapDbm.clear();

                h.postDelayed(this, avgDelay);
            }
        };

        h.postDelayed(runAvg, avgDelay);
    }

    private void getMode() {
        runMode = new Runnable() {
            @Override
            public void run(){

                if(listRI.size() > 0 && listRI.isEmpty())
                    Log.d(TAG, "There are no known access point !");

                for(int i=0;i<listRI.size();i++) {
                    RouterInfo ri = listRI.get(i);

                    ArrayList<Double> tempArr = linkmapDbm.get(ri.getBssid());
                    Double signal = countMode(tempArr);
                    ri.setSignalLvl(signal);

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

                    Log.d(TAG, "Mode signal for " + ri.getSsid() + " is " + signal + ", The distance after mode is : " + new DecimalFormat("#.##").format(calculateDistance(signal, 2437)) + "m.");
                }

                if(sendAllow) {
                    sendCoordinate();
                }

                listRI = new LinkedList<>();
                linkmapDbm.clear();

                h.postDelayed(this, modeDelay);
            }
        };

        h.postDelayed(runMode, modeDelay);
    }

    private void kalmanfilter() {

        runKalman = new Runnable() {
            @Override
            public void run() {
                if (listRI.size() > 0 && listRI.isEmpty())
                    Log.d(TAG, "There are no known access point !");

                for (int i = 0; i < listRI.size(); i++) {
                    RouterInfo ri = listRI.get(i);

                    ArrayList<Double> tempArr = linkmapDbm.get(ri.getBssid());
                    //tempArr = filter(tempArr); // Filter 10% on the top and bottom of the list

                    //Double signal = kalmanCalculator(tempArr);  // Apply KalmanFilter and return the average
                    //Double signal = getLowestRssi(tempArr); // Get the lowest Rssi value from the array list
                    //ri.setSignalLvl(signal);

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

//                    Log.d(TAG, "Mode signal for " + ri.getSsid() + " is " + signal + ", The distance after kalmanfilter is : " + new DecimalFormat("#.##").format(calculateDistance(signal, 2437)) + "m.");
                }

                if(sendAllow) {
                    sendCoordinate();
                }

                listRI = new LinkedList<>();
                linkmapDbm.clear();

                h.postDelayed(this, modeDelay);
            }
        };
        h.postDelayed(runKalman, modeDelay);
    }

    private void getDbm() {
//        wifiMgr.startScan();
        runDbm = new Runnable() {
            @Override
            public void run() {
                wifiMgr.startScan();
//                int state = wifiMgr.getWifiState();
//                WifiInfo info = wifiMgr.getConnectionInfo(); // Get the connected wifiMgr info
//                List<ScanResult> result = wifiMgr.getScanResults();
//
//                txtWifiInfoResult.setText("Wifi info: " + info);
//                txtWifiStateResult.setText("Wifi state: " + state);
//                Log.d(TAG, "Scan result: " + result);
//
//                txtResult.setText("Result\n");// Reset text view
//
//                for(int i = 0;i<result.size();i++) {
//                    String ssid = result.get(i).SSID;
//                    String bssid = result.get(i).BSSID;
//                    double wifiDb = result.get(i).level;
//                    int level = wifiMgr.calculateSignalLevel((int)wifiDb, 10);
//                    double frequency = result.get(i).frequency;
//                    double distance = calculateDistance(wifiDb, frequency);
//
////                    if(ssid.equals("IPS_AP6") || ssid.equals("IPS_AP5") || ssid.equals("IPS_AP4")) {
////                    if(ssid.equals("Redmi222") || ssid.equals("Jycans") || ssid.equals("PiAP") || ssid.equals("PiJeff") || ssid.equals("MoonPiAP") || ssid.equals("PiGary") || ssid.equals("LenovoS650")) {
//                    if(ssid.contains("IPS_AP") || ssid.contains("IPS-AP")) {
//                        txtResult.append("SSID: " + ssid + "\n");
//                        txtResult.append("BSSID: " + bssid + "\n");
//                        txtResult.append("Level: " + wifiDb + "\n");
//                        txtResult.append("Frequency: " + frequency + "\n");
//                        txtResult.append("Signal Level: " + level + "\n");
//                        txtResult.append("Distance: " + new DecimalFormat("#.##").format(distance) + " m\n\n");
//
//                        Log.d(TAG, "Distance before average: " + new DecimalFormat("#.##").format(distance) + " m, RRSI : " + wifiDb);
//
//                        RouterInfo ri = new RouterInfo(ssid, bssid, frequency, wifiDb, level);
//
//                        if(!linkmapDbm.containsKey(ri.getBssid())) {
//                            listRI.add(ri);
//                            ArrayList<Double> tempArr = new ArrayList<>();
//                            tempArr.add(ri.getSignalLvl());
//
//                            linkmapDbm.put(ri.getBssid(), tempArr);
//                        } else {
//                            ArrayList<Double> tempArr = linkmapDbm.get(ri.getBssid());
//                            tempArr.add(ri.getSignalLvl());
//
//                            linkmapDbm.put(ri.getBssid(), tempArr);
//                        }
//                    }
//                }
//
                h.postDelayed(this, totalDelay);
            }
        };

        h.postDelayed(runDbm, totalDelay);
    }

    private Double totalCalculation(ArrayList<Double> list) {
        Double total = 0.0;

        for(int i=0;i<list.size();i++) {
            Double temp = Math.pow(10, (list.size()/10));
            //total += list.get(i);
            total += temp;
        }
        Log.d(TAG, "Total : " + total + "");

        return total;
    }

    private Double countMode(ArrayList<Double> list) {
        ArrayList<Double> tempArr = new ArrayList<>();
        LinkedHashMap<Double, ArrayList<Object>> counter = new LinkedHashMap<>();

        for(int i = 0;i<list.size();i++) {
            if(!tempArr.contains(list.get(i)))
                tempArr.add(list.get(i));
        }

        for(int i=0;i<list.size();i++) {
            if(counter.containsKey(list.get(i))) {
                ArrayList<Object> temp = counter.get(list.get(i));
                Integer c = (Integer)temp.get(1);
                c+=1;
                temp.set(1, c);
                counter.put(list.get(i), temp);
            } else {
                ArrayList<Object> temp = new ArrayList<>();
                temp.add(list.get(i));
                temp.add(1);
                counter.put(list.get(i), temp);
            }
        }

        Double signal = (Double)counter.get(tempArr.get(0)).get(0);
        Integer c = (Integer)counter.get(tempArr.get(0)).get(1);

        for(int i=1;i<tempArr.size();i++) {
            if(c <= (Integer)counter.get(tempArr.get(i)).get(1)) {
                c = (Integer)counter.get(tempArr.get(i)).get(1);
                signal = (Double)counter.get(tempArr.get(i)).get(0);
            }
        }

        return signal;
    }

    private ArrayList<Double> sortRssi(ArrayList<Double> temp) {
        Collections.sort(temp, new Comparator<Double>() {
            @Override
            public int compare(Double val1, Double val2) {
                return val1.compareTo(val2);
            }
        });
        Collections.reverse(temp);

        return temp;
    }

    private Double getLowestRssi(ArrayList<Double> temp) {
        temp = sortRssi(temp);
        return temp.get(0);
    }

    private ArrayList<Double> filter(ArrayList<Double> temp) {

        Double toRemove = Math.floor(Double.parseDouble(temp.size() + "")/10);
        Log.d(TAG, "Before sort : " + temp.toString());

        // Remove the bottom 10% of the ArrayList
        for(int count=0;count<toRemove;count++) {
            temp.remove(0);
        }

        // Remove the top 10% of the ArrayList
        for(int count=0;count<toRemove;count++) {
            temp.remove(temp.size()-1);
        }

        Log.d(TAG, "After sort : " + temp.toString());

        return temp;
    }

    private Double kalmanCalculator(ArrayList<Double> signalList) {
        Double initialSignalStrength = Math.abs(signalList.get(0));
        Double kalmanGain;
        Double errorEst = 2.0;
        Double errorMea = 4.0;

        for(int i = 1;i<signalList.size();i++) {
            kalmanGain = errorEst / (errorEst + errorMea);
            initialSignalStrength = initialSignalStrength + kalmanGain * (Math.abs(signalList.get(i)) - initialSignalStrength);
            errorEst = (1 - kalmanGain) * errorEst;
        }

        return -1*initialSignalStrength;
    }

    /**
     * Formula:
     *
     * distance = ToA ∗ c;
     * ToA; where ToA = Unknown
     * c; where c = 3x10^5
     *
     * 𝑅𝑆𝑆𝐼 = − 10𝑛 𝑙𝑜𝑔10 𝑑 + 𝐴
     * n = -((RSSI - A)/(10(log10 x d)))
     * Variable:
     * A; where A = A is the received signal strength at 1 meter distance, A is obtained in a no-obstacle one-meter distance signal strength measurements from the reference nodes.
     * d; where d = d is the distance from sender;
     * n; where n = 2(Free space), 2.7~3.5(Urban area Cellular radio), 1.6~1.8(In Building LOS), 3~5(Shadowed Urban Area cellular radio)
     *
     * @param signalLevelInDb
     * @param freqInMHz
     * @return the distance from AP to user in meter
     */
    private double calculateDistance(double signalLevelInDb, double freqInMHz) {
        double exp = (27.55d - (20d * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0; // We can see that values up to -50 dBm, can be used for somewhat relevant calculation. Lower values are highly unstable.
        return Math.pow(10.0, exp);
    }
}
