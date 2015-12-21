package com.georgegalt.tinysqueezeplayer;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity.java";

    private String macAddress;
    ServerInfo serverInfo;
    private SlimProto slimProto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        serverInfo = new ServerInfo(this);
        slimProto = new SlimProto();
        GetConnectedState();
    }


    public SlimProto getSlimProto() {
        return slimProto;
    }

    // find out if we are on WiFi and get MAC address
    private boolean GetConnectedState() {
        boolean isWiFi = false;
        try {
            ConnectivityManager cm =
                    (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
            isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;

            WifiManager wifi = (WifiManager) this
                    .getSystemService(this.WIFI_SERVICE);
            wifi.setWifiEnabled(true);
            WifiInfo info = wifi.getConnectionInfo();
            macAddress = info.getMacAddress();

            if (macAddress == null) {
                Toast.makeText(this, "Null " + isConnected + ":" + info.toString(), Toast.LENGTH_LONG).show();
                Log.d(TAG, info.toString());
            } else {
                Toast.makeText(this, macAddress, Toast.LENGTH_LONG).show();
                ServerInfo.setThisPlayerID(macAddress);
            }
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
        return isWiFi;
    }



}
