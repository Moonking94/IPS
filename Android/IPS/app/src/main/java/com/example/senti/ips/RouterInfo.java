package com.example.senti.ips;

/**
 * Created by Senti on 3/28/2017.
 */

public class RouterInfo {

    private String ssid;
    private String bssid;
    private double frequency;
    private double signalLvl;

    public RouterInfo() { }

    public RouterInfo(String ssid, String bssid, double frequency, double signalLvl) {
        this.ssid = ssid;
        this.bssid = bssid;
        this.frequency = frequency;
        this.signalLvl = signalLvl;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public double getSignalLvl() {
        return signalLvl;
    }

    public void setSignalLvl(double signalLvl) { this.signalLvl = signalLvl; }
}
