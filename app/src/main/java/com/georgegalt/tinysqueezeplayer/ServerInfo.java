package com.georgegalt.tinysqueezeplayer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.List;

/**
 * Created by ggalt on 12/17/15.
 */
public class ServerInfo {
    private static final String TAG = "ServerInfo-Class";

    private static final String DEFAULT_SERVER_IP = "127.0.0.1";
    private static final String DEFAULT_PLAYER_PORT = "3448";
    private static final String DEFAULT_CLI_PORT = "9090";
    private static final String DEFAULT_WEB_PORT = "9000";
    private static final String DEFAULT_PLAYER_NAME = "SqueezeMe";
    private static final String DEFAULT_USERNAME = "";
    private static final String DEFAULT_PASSWORD = "";

    private static String SERVER_IP;
    private static String PLAYER_PORT;
    private static String CLI_PORT;
    private static String WEB_PORT;
    private static String PLAYER_NAME;
    private static String USERNAME;
    private static String PASSWORD;

    /*
     * These are volatile from one session to the next and son
     * we don't store them in the preferences but gather them
     * through server calls.
     */
    private static String albumCount;
    private static String artistCount;
    private static String genreCount;
    private static String songCount;

    private static List<String> favorites;

    private static List<PlayerInfo> players;

    private static String thisPlayerID;     // mac address for this device

    private SharedPreferences prefs;
    // if false, we can't write to preference file because we don't have a context
    private boolean bCanUpdate = false;

    public ServerInfo(Activity activity) {
        prefs = PreferenceManager.getDefaultSharedPreferences(activity.getBaseContext());
        bCanUpdate = true;      // we have a context so allow updating
        readValues();
    }

    public static String getServerIP() { return SERVER_IP;}
    public static String getPlayerPort() { return PLAYER_PORT; }
    public static String getCliPort() { return CLI_PORT; }
    public static String getWebPort() { return WEB_PORT; }
    public static String getPlayerName() { return PLAYER_NAME; }
    public static String getUSERNAME() { return USERNAME; }
    public static String getPASSWORD() { return PASSWORD; }

    public static List<String> getFavorites() {return favorites;}
    public static List<PlayerInfo> getPlayers() {return players;}

    public static String getAlbumCount() {return albumCount;}
    public static String getArtistCount() {return artistCount;}
    public static String getGenreCount() {return genreCount;}
    public static String getSongCount() {return songCount;}

    public static String getThisPlayerID() {
        return thisPlayerID;
    }

    public static void setServerIP(String s) {SERVER_IP=s;}
    public static void setPlayerPort(String s) {PLAYER_PORT=s;}
    public static void setCliPort(String s) {CLI_PORT=s;}
    public static void setWebPort(String s) {WEB_PORT=s;}
    public static void setPlayerName(String s) {PLAYER_NAME=s;}
    public static void setUSERNAME(String s) {USERNAME=s;}
    public static void setPASSWORD(String s) {PASSWORD=s;}

    public static void setAlbumCount(String s) {albumCount = s;}
    public static void setArtistCount(String s) {artistCount = s;}
    public static void setGenreCount(String s) {genreCount = s;}
    public static void setSongCount(String s) { songCount = s;}

    public boolean isbCanUpdate() {return bCanUpdate;}

    public static void setThisPlayerID(String thisPlayerID) {
        ServerInfo.thisPlayerID = thisPlayerID;
    }

    public void resetValues(){
        SERVER_IP = DEFAULT_SERVER_IP;
        PLAYER_PORT = DEFAULT_PLAYER_PORT;
        CLI_PORT = DEFAULT_CLI_PORT;
        WEB_PORT = DEFAULT_WEB_PORT;
        PLAYER_NAME = DEFAULT_PLAYER_NAME;
        USERNAME = DEFAULT_USERNAME;
        PASSWORD = DEFAULT_PASSWORD;
    }

    public void readValues(){
        if(bCanUpdate){
            SERVER_IP = prefs.getString("SERVER_IP_ID", DEFAULT_SERVER_IP);
            PLAYER_PORT = prefs.getString("PLAYER_PORT_ID", DEFAULT_PLAYER_PORT );
            CLI_PORT = prefs.getString("CLI_PORT_ID", DEFAULT_CLI_PORT);
            WEB_PORT = prefs.getString("WEB_PORT_ID", DEFAULT_WEB_PORT);
            PLAYER_NAME = prefs.getString("PLAYER_NAME_ID", DEFAULT_PLAYER_NAME);
            USERNAME = prefs.getString("USERNAME_ID", DEFAULT_USERNAME);
            PASSWORD = prefs.getString("PASSWORD_ID", DEFAULT_PASSWORD);
        }
    }

    public void writeValues(){
        if(bCanUpdate){
            SharedPreferences.Editor editor = prefs.edit();

            editor.putString("SERVER_IP_ID", SERVER_IP);
            editor.putString("PLAYER_PORT_ID", PLAYER_PORT);
            editor.putString("CLI_PORT_ID", CLI_PORT);
            editor.putString("WEB_PORT_ID", WEB_PORT);
            editor.putString("PLAYER_NAME_ID", PLAYER_NAME);
            editor.putString("USERNAME_ID", USERNAME);
            editor.putString("PASSWORD_ID", PASSWORD);
            editor.apply();
        }
    }
}

class PlayerInfo {
    private String name;
    private String id;
    private String macAddress;
    private String ipAddress;

    PlayerInfo(){

    }

    PlayerInfo(String name, String id, String macAddress, String ipAddress) {
        this.name = name;
        this.id = id;
        this.macAddress = macAddress;
        this.ipAddress = ipAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
