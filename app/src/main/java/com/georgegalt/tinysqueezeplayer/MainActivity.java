package com.georgegalt.tinysqueezeplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity.java";
    public static final int SETUP_REQUEST_CODE = 0;

    private final static int INIT = 0;
    private final static int CONNECTED = 1;
    private final static int DISCONNECTED = 2;
    private int state = INIT;

    private String macAddress;
    ServerInfo serverInfo;
    private SlimProto slimProto;
    private PlayerService playerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        serverInfo = new ServerInfo(this);
        if( !ServerInfo.getUserReview()) {
            // user has not set up the Server IP etc.  Stop and get info
            launchSetup();
        } else {
            Startup();
        }
    }

    /*
     * Interaction with Options Menu
     */
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return launchSetup();
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
    private boolean launchSetup() {
        Intent intent = new Intent(this,SetupActivity.class);
        startActivityForResult(intent, SETUP_REQUEST_CODE);
        return true;
    }

    private void Startup() {
        // We don't want to connect over a data network, WiFi only!!
        if( GetConnectedState() ){
            try {

                slimProto = new SlimProto();
                playerService = new PlayerService(this);
                slimProto.connect(InetAddress.getByName(ServerInfo.getServerIP()), Integer.getInteger(ServerInfo.getPlayerPort()));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "No WiFi Connection.  Exiting.", Toast.LENGTH_LONG).show();
        }
    }

    /*
     * Code for managing return from Setup Activity
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK && requestCode == SETUP_REQUEST_CODE){
            Bundle extras = data.getExtras();
            if(extras.getBoolean("DataUpdated", false)) {
                Toast.makeText(getApplicationContext(), R.string.settings_updated_msg, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(),R.string.settings_cancel_msg,Toast.LENGTH_SHORT).show();
            }
        }
        Startup();
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
